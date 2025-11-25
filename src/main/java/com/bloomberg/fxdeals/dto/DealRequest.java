package com.bloomberg.fxdeals.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for incoming FX Deal requests
 * 
 * Contains validation annotations to ensure data integrity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealRequest {

    @NotBlank(message = "Deal Unique Id is required")
    @Size(max = 100, message = "Deal Unique Id must not exceed 100 characters")
    private String dealUniqueId;

    @NotBlank(message = "From Currency ISO Code is required")
    @Size(min = 3, max = 3, message = "From Currency ISO Code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "From Currency ISO Code must be 3 uppercase letters")
    private String fromCurrencyIsoCode;

    @NotBlank(message = "To Currency ISO Code is required")
    @Size(min = 3, max = 3, message = "To Currency ISO Code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "To Currency ISO Code must be 3 uppercase letters")
    private String toCurrencyIsoCode;

    @NotNull(message = "Deal timestamp is required")
    @PastOrPresent(message = "Deal timestamp cannot be in the future")
    private LocalDateTime dealTimestamp;

    @NotNull(message = "Deal amount is required")
    @DecimalMin(value = "0.0001", message = "Deal amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Deal amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal dealAmount;
}

