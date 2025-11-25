package com.bloomberg.fxdeals.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk deal import requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkDealRequest {

    @NotNull(message = "Deals list is required")
    @NotEmpty(message = "Deals list cannot be empty")
    @Valid
    private List<DealRequest> deals;
}

