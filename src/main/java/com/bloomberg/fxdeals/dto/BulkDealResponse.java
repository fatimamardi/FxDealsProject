package com.bloomberg.fxdeals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for bulk deal import responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkDealResponse {

    private int totalReceived;
    private int successfullyImported;
    private int skippedDuplicates;
    private int failed;
    private List<String> errors = new ArrayList<>();
    private List<DealResponse> importedDeals = new ArrayList<>();
}

