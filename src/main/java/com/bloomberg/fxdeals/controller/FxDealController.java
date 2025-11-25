package com.bloomberg.fxdeals.controller;

import com.bloomberg.fxdeals.dto.BulkDealRequest;
import com.bloomberg.fxdeals.dto.BulkDealResponse;
import com.bloomberg.fxdeals.dto.DealRequest;
import com.bloomberg.fxdeals.dto.DealResponse;
import com.bloomberg.fxdeals.service.FxDealService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for FX Deal operations
 * 
 * Provides endpoints for:
 * - Single deal import
 * - Bulk deal import
 * - Deal retrieval
 */
@RestController
@RequestMapping("/api/v1/deals")
@CrossOrigin(origins = "*")
public class FxDealController {

    private static final Logger logger = LoggerFactory.getLogger(FxDealController.class);

    private final FxDealService dealService;

    @Autowired
    public FxDealController(FxDealService dealService) {
        this.dealService = dealService;
    }

    /**
     * Import a single FX deal
     * 
     * POST /api/v1/deals
     * 
     * @param dealRequest the deal to import
     * @return DealResponse with imported deal information
     */
    @PostMapping
    public ResponseEntity<?> importDeal(@Valid @RequestBody DealRequest dealRequest) {
        logger.info("Received request to import deal: {}", dealRequest.getDealUniqueId());

        try {
            DealResponse response = dealService.importDeal(dealRequest);
            logger.info("Successfully imported deal: {}", response.getDealUniqueId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error importing deal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));

        } catch (IllegalStateException e) {
            logger.warn("Duplicate deal detected: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_DEAL", e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error importing deal: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Failed to import deal: " + e.getMessage()));
        }
    }

    /**
     * Import multiple FX deals in bulk
     * 
     * POST /api/v1/deals/bulk
     * 
     * @param bulkRequest the bulk deal request
     * @return BulkDealResponse with import statistics
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkDealResponse> importDealsBulk(@Valid @RequestBody BulkDealRequest bulkRequest) {
        logger.info("Received request to import {} deals in bulk", bulkRequest.getDeals().size());

        BulkDealResponse response = dealService.importDealsBulk(bulkRequest.getDeals());
        
        HttpStatus status = response.getFailed() == 0 && response.getSkippedDuplicates() == 0
            ? HttpStatus.CREATED
            : response.getSuccessfullyImported() > 0
                ? HttpStatus.PARTIAL_CONTENT
                : HttpStatus.BAD_REQUEST;

        logger.info("Bulk import completed with status {}: {} imported, {} duplicates, {} failed",
            status, response.getSuccessfullyImported(), response.getSkippedDuplicates(), response.getFailed());

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Get all deals
     * 
     * GET /api/v1/deals
     * 
     * @return list of all deals
     */
    @GetMapping
    public ResponseEntity<List<DealResponse>> getAllDeals() {
        logger.debug("Received request to retrieve all deals");
        List<DealResponse> deals = dealService.getAllDeals();
        return ResponseEntity.ok(deals);
    }

    /**
     * Get a deal by unique ID
     * 
     * GET /api/v1/deals/{dealUniqueId}
     * 
     * @param dealUniqueId the unique deal identifier
     * @return DealResponse if found
     */
    @GetMapping("/{dealUniqueId}")
    public ResponseEntity<DealResponse> getDealByUniqueId(@PathVariable String dealUniqueId) {
        logger.debug("Received request to retrieve deal: {}", dealUniqueId);
        DealResponse deal = dealService.getDealByUniqueId(dealUniqueId);
        
        if (deal == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(deal);
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;

        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

