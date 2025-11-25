package com.bloomberg.fxdeals.service;

import com.bloomberg.fxdeals.dto.DealRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating FX Deal data
 * 
 * Performs comprehensive validation including:
 * - Required field checks
 * - Data type validation
 * - Format validation
 * - Business rule validation
 */
@Service
public class DealValidationService {

    private static final Logger logger = LoggerFactory.getLogger(DealValidationService.class);
    
    // ISO 4217 currency code pattern (3 uppercase letters)
    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    
    // Common valid currency codes (subset of ISO 4217)
    private static final List<String> VALID_CURRENCY_CODES = List.of(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "HKD", "NZD",
        "SEK", "NOK", "DKK", "PLN", "ZAR", "SGD", "MXN", "INR", "BRL", "KRW"
    );

    /**
     * Validate a single deal request
     * 
     * @param dealRequest the deal request to validate
     * @return list of validation errors (empty if valid)
     */
    public List<String> validateDeal(DealRequest dealRequest) {
        List<String> errors = new ArrayList<>();

        if (dealRequest == null) {
            errors.add("Deal request cannot be null");
            return errors;
        }

        // Validate Deal Unique Id
        validateDealUniqueId(dealRequest.getDealUniqueId(), errors);

        // Validate From Currency ISO Code
        validateCurrencyCode(dealRequest.getFromCurrencyIsoCode(), "From Currency", errors);

        // Validate To Currency ISO Code
        validateCurrencyCode(dealRequest.getToCurrencyIsoCode(), "To Currency", errors);

        // Validate currency codes are different
        if (dealRequest.getFromCurrencyIsoCode() != null && 
            dealRequest.getToCurrencyIsoCode() != null &&
            dealRequest.getFromCurrencyIsoCode().equals(dealRequest.getToCurrencyIsoCode())) {
            errors.add("From Currency and To Currency must be different");
        }

        // Validate Deal Timestamp
        validateDealTimestamp(dealRequest.getDealTimestamp(), errors);

        // Validate Deal Amount
        validateDealAmount(dealRequest.getDealAmount(), errors);

        if (!errors.isEmpty()) {
            logger.warn("Validation failed for deal {}: {}", 
                dealRequest.getDealUniqueId(), errors);
        }

        return errors;
    }

    /**
     * Validate deal unique ID
     */
    private void validateDealUniqueId(String dealUniqueId, List<String> errors) {
        if (dealUniqueId == null || dealUniqueId.trim().isEmpty()) {
            errors.add("Deal Unique Id is required and cannot be empty");
        } else if (dealUniqueId.length() > 100) {
            errors.add("Deal Unique Id must not exceed 100 characters");
        } else if (dealUniqueId.trim().length() != dealUniqueId.length()) {
            errors.add("Deal Unique Id cannot have leading or trailing whitespace");
        }
    }

    /**
     * Validate currency ISO code
     */
    private void validateCurrencyCode(String currencyCode, String fieldName, List<String> errors) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            errors.add(fieldName + " ISO Code is required");
            return;
        }

        String trimmed = currencyCode.trim().toUpperCase();
        
        if (trimmed.length() != 3) {
            errors.add(fieldName + " ISO Code must be exactly 3 characters");
            return;
        }

        if (!CURRENCY_CODE_PATTERN.matcher(trimmed).matches()) {
            errors.add(fieldName + " ISO Code must be 3 uppercase letters (A-Z)");
            return;
        }

        // Optional: Validate against known currency codes
        // This can be made configurable or removed if all ISO codes should be accepted
        if (!VALID_CURRENCY_CODES.contains(trimmed)) {
            logger.debug("Currency code {} is not in the common list, but format is valid", trimmed);
        }
    }

    /**
     * Validate deal timestamp
     */
    private void validateDealTimestamp(LocalDateTime dealTimestamp, List<String> errors) {
        if (dealTimestamp == null) {
            errors.add("Deal timestamp is required");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (dealTimestamp.isAfter(now)) {
            errors.add("Deal timestamp cannot be in the future");
        }

        // Optional: Validate timestamp is not too old (e.g., more than 10 years)
        LocalDateTime tenYearsAgo = now.minusYears(10);
        if (dealTimestamp.isBefore(tenYearsAgo)) {
            errors.add("Deal timestamp is too old (more than 10 years)");
        }
    }

    /**
     * Validate deal amount
     */
    private void validateDealAmount(BigDecimal dealAmount, List<String> errors) {
        if (dealAmount == null) {
            errors.add("Deal amount is required");
            return;
        }

        if (dealAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Deal amount must be greater than 0");
            return;
        }

        if (dealAmount.scale() > 4) {
            errors.add("Deal amount cannot have more than 4 decimal places");
        }

        // Check for reasonable maximum value (e.g., 1 trillion)
        BigDecimal maxAmount = new BigDecimal("1000000000000");
        if (dealAmount.compareTo(maxAmount) > 0) {
            errors.add("Deal amount exceeds maximum allowed value");
        }
    }

    /**
     * Validate multiple deals
     * 
     * @param deals list of deal requests to validate
     * @return map of deal index to validation errors
     */
    public List<String> validateDeals(List<DealRequest> deals) {
        List<String> allErrors = new ArrayList<>();

        if (deals == null || deals.isEmpty()) {
            allErrors.add("Deals list cannot be null or empty");
            return allErrors;
        }

        for (int i = 0; i < deals.size(); i++) {
            DealRequest deal = deals.get(i);
            List<String> errors = validateDeal(deal);
            
            if (!errors.isEmpty()) {
                for (String error : errors) {
                    allErrors.add(String.format("Deal[%d] (%s): %s", 
                        i, 
                        deal != null && deal.getDealUniqueId() != null ? deal.getDealUniqueId() : "unknown",
                        error));
                }
            }
        }

        return allErrors;
    }
}

