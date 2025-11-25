package com.bloomberg.fxdeals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for FX Deal responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealResponse {

    private Long id;
    private String dealUniqueId;
    private String fromCurrencyIsoCode;
    private String toCurrencyIsoCode;
    private LocalDateTime dealTimestamp;
    private BigDecimal dealAmount;
    private LocalDateTime createdAt;
}

