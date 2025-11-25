package com.bloomberg.fxdeals.service;

import com.bloomberg.fxdeals.dto.BulkDealResponse;
import com.bloomberg.fxdeals.dto.DealRequest;
import com.bloomberg.fxdeals.dto.DealResponse;
import com.bloomberg.fxdeals.model.FxDeal;
import com.bloomberg.fxdeals.repository.FxDealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing FX Deal operations
 * 
 * Handles deal import with:
 * - Validation
 * - Duplicate detection
 * - No-rollback transaction handling (each deal saved independently)
 */
@Service
public class FxDealService {

    private static final Logger logger = LoggerFactory.getLogger(FxDealService.class);

    private final FxDealRepository dealRepository;
    private final DealValidationService validationService;

    @Autowired
    public FxDealService(FxDealRepository dealRepository, DealValidationService validationService) {
        this.dealRepository = dealRepository;
        this.validationService = validationService;
    }

    /**
     * Import a single deal
     * 
     * @param dealRequest the deal to import
     * @return DealResponse with imported deal information
     * @throws IllegalArgumentException if validation fails or deal already exists
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public DealResponse importDeal(DealRequest dealRequest) {
        logger.info("Importing deal with unique ID: {}", dealRequest.getDealUniqueId());

        // Validate the deal
        List<String> validationErrors = validationService.validateDeal(dealRequest);
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join("; ", validationErrors);
            logger.error("Validation failed for deal {}: {}", dealRequest.getDealUniqueId(), errorMessage);
            throw new IllegalArgumentException("Validation failed: " + errorMessage);
        }

        // Check for duplicate
        if (dealRepository.existsByDealUniqueId(dealRequest.getDealUniqueId())) {
            logger.warn("Deal with unique ID {} already exists, skipping import", dealRequest.getDealUniqueId());
            throw new IllegalStateException("Deal with unique ID " + dealRequest.getDealUniqueId() + " already exists");
        }

        // Convert and save
        FxDeal deal = convertToEntity(dealRequest);
        try {
            FxDeal savedDeal = dealRepository.save(deal);
            logger.info("Successfully imported deal with unique ID: {}", savedDeal.getDealUniqueId());
            return convertToResponse(savedDeal);
        } catch (Exception e) {
            logger.error("Error saving deal {}: {}", dealRequest.getDealUniqueId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save deal: " + e.getMessage(), e);
        }
    }

    /**
     * Import multiple deals in bulk
     * 
     * Uses REQUIRES_NEW propagation to ensure each deal is saved independently
     * No rollback is allowed - every valid deal is persisted
     * 
     * @param dealRequests list of deals to import
     * @return BulkDealResponse with import statistics
     */
    public BulkDealResponse importDealsBulk(List<DealRequest> dealRequests) {
        logger.info("Starting bulk import of {} deals", dealRequests.size());

        BulkDealResponse response = BulkDealResponse.builder()
            .totalReceived(dealRequests.size())
            .successfullyImported(0)
            .skippedDuplicates(0)
            .failed(0)
            .errors(new ArrayList<>())
            .importedDeals(new ArrayList<>())
            .build();

        // Track processed deal IDs to detect duplicates within the batch
        Set<String> processedInBatch = new HashSet<>();

        for (int i = 0; i < dealRequests.size(); i++) {
            DealRequest dealRequest = dealRequests.get(i);
            String dealId = dealRequest.getDealUniqueId();

            try {
                // Check for duplicate within the batch
                if (processedInBatch.contains(dealId)) {
                    String error = String.format("Deal[%d] (%s): Duplicate deal ID in the same batch", i, dealId);
                    response.getErrors().add(error);
                    response.setFailed(response.getFailed() + 1);
                    logger.warn(error);
                    continue;
                }

                // Import the deal (each in its own transaction)
                DealResponse importedDeal = importDeal(dealRequest);
                response.getImportedDeals().add(importedDeal);
                response.setSuccessfullyImported(response.getSuccessfullyImported() + 1);
                processedInBatch.add(dealId);
                logger.debug("Successfully imported deal[{}]: {}", i, dealId);

            } catch (IllegalStateException e) {
                // Duplicate deal (already exists in DB)
                String error = String.format("Deal[%d] (%s): %s", i, dealId, e.getMessage());
                response.getErrors().add(error);
                response.setSkippedDuplicates(response.getSkippedDuplicates() + 1);
                logger.warn(error);

            } catch (IllegalArgumentException e) {
                // Validation error
                String error = String.format("Deal[%d] (%s): %s", i, dealId, e.getMessage());
                response.getErrors().add(error);
                response.setFailed(response.getFailed() + 1);
                logger.warn(error);

            } catch (Exception e) {
                // Other errors
                String error = String.format("Deal[%d] (%s): Unexpected error - %s", i, dealId, e.getMessage());
                response.getErrors().add(error);
                response.setFailed(response.getFailed() + 1);
                logger.error("Unexpected error importing deal[{}] {}: {}", i, dealId, e.getMessage(), e);
            }
        }

        logger.info("Bulk import completed. Total: {}, Imported: {}, Duplicates: {}, Failed: {}",
            response.getTotalReceived(),
            response.getSuccessfullyImported(),
            response.getSkippedDuplicates(),
            response.getFailed());

        return response;
    }

    /**
     * Get all deals
     * 
     * @return list of all deals
     */
    public List<DealResponse> getAllDeals() {
        logger.debug("Retrieving all deals");
        return dealRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a deal by unique ID
     * 
     * @param dealUniqueId the unique deal identifier
     * @return DealResponse if found
     */
    public DealResponse getDealByUniqueId(String dealUniqueId) {
        logger.debug("Retrieving deal with unique ID: {}", dealUniqueId);
        return dealRepository.findByDealUniqueId(dealUniqueId)
            .map(this::convertToResponse)
            .orElse(null);
    }

    /**
     * Convert DealRequest to FxDeal entity
     */
    private FxDeal convertToEntity(DealRequest request) {
        return FxDeal.builder()
            .dealUniqueId(request.getDealUniqueId().trim())
            .fromCurrencyIsoCode(request.getFromCurrencyIsoCode().trim().toUpperCase())
            .toCurrencyIsoCode(request.getToCurrencyIsoCode().trim().toUpperCase())
            .dealTimestamp(request.getDealTimestamp())
            .dealAmount(request.getDealAmount())
            .build();
    }

    /**
     * Convert FxDeal entity to DealResponse
     */
    private DealResponse convertToResponse(FxDeal deal) {
        return DealResponse.builder()
            .id(deal.getId())
            .dealUniqueId(deal.getDealUniqueId())
            .fromCurrencyIsoCode(deal.getFromCurrencyIsoCode())
            .toCurrencyIsoCode(deal.getToCurrencyIsoCode())
            .dealTimestamp(deal.getDealTimestamp())
            .dealAmount(deal.getDealAmount())
            .createdAt(deal.getCreatedAt())
            .build();
    }
}

