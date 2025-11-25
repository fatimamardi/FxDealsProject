package com.bloomberg.fxdeals.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an FX Deal in the data warehouse
 * 
 * This entity stores all deal information including unique identifier,
 * currency codes, timestamp, and amount.
 */
@Entity
@Table(name = "fx_deals", 
       uniqueConstraints = @UniqueConstraint(columnNames = "dealUniqueId"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_unique_id", nullable = false, unique = true, length = 100)
    private String dealUniqueId;

    @Column(name = "from_currency_iso_code", nullable = false, length = 3)
    private String fromCurrencyIsoCode;

    @Column(name = "to_currency_iso_code", nullable = false, length = 3)
    private String toCurrencyIsoCode;

    @Column(name = "deal_timestamp", nullable = false)
    private LocalDateTime dealTimestamp;

    @Column(name = "deal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal dealAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

