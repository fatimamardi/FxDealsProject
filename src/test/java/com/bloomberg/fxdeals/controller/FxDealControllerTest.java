package com.bloomberg.fxdeals.controller;

import com.bloomberg.fxdeals.dto.BulkDealRequest;
import com.bloomberg.fxdeals.dto.BulkDealResponse;
import com.bloomberg.fxdeals.dto.DealRequest;
import com.bloomberg.fxdeals.dto.DealResponse;
import com.bloomberg.fxdeals.service.FxDealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FxDealController
 */
@WebMvcTest(FxDealController.class)
class FxDealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FxDealService dealService;

    @Autowired
    private ObjectMapper objectMapper;

    private DealRequest validDealRequest;
    private DealResponse dealResponse;

    @BeforeEach
    void setUp() {
        validDealRequest = new DealRequest();
        validDealRequest.setDealUniqueId("DEAL-001");
        validDealRequest.setFromCurrencyIsoCode("USD");
        validDealRequest.setToCurrencyIsoCode("EUR");
        validDealRequest.setDealTimestamp(LocalDateTime.now().minusHours(1));
        validDealRequest.setDealAmount(new BigDecimal("1000.50"));

        dealResponse = DealResponse.builder()
            .id(1L)
            .dealUniqueId("DEAL-001")
            .fromCurrencyIsoCode("USD")
            .toCurrencyIsoCode("EUR")
            .dealTimestamp(validDealRequest.getDealTimestamp())
            .dealAmount(validDealRequest.getDealAmount())
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void testImportDeal_ValidRequest_ReturnsCreated() throws Exception {
        when(dealService.importDeal(any(DealRequest.class))).thenReturn(dealResponse);

        mockMvc.perform(post("/api/v1/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDealRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealUniqueId").value("DEAL-001"))
                .andExpect(jsonPath("$.fromCurrencyIsoCode").value("USD"))
                .andExpect(jsonPath("$.toCurrencyIsoCode").value("EUR"));
    }

    @Test
    void testImportDeal_ValidationError_ReturnsBadRequest() throws Exception {
        when(dealService.importDeal(any(DealRequest.class)))
            .thenThrow(new IllegalArgumentException("Validation failed: Invalid currency"));

        mockMvc.perform(post("/api/v1/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDealRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testImportDeal_DuplicateDeal_ReturnsConflict() throws Exception {
        when(dealService.importDeal(any(DealRequest.class)))
            .thenThrow(new IllegalStateException("Deal already exists"));

        mockMvc.perform(post("/api/v1/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDealRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_DEAL"));
    }

    @Test
    void testImportDealsBulk_ValidRequest_ReturnsCreated() throws Exception {
        BulkDealRequest bulkRequest = new BulkDealRequest();
        bulkRequest.setDeals(List.of(validDealRequest));

        BulkDealResponse bulkResponse = BulkDealResponse.builder()
            .totalReceived(1)
            .successfullyImported(1)
            .skippedDuplicates(0)
            .failed(0)
            .importedDeals(List.of(dealResponse))
            .build();

        when(dealService.importDealsBulk(any(List.class))).thenReturn(bulkResponse);

        mockMvc.perform(post("/api/v1/deals/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalReceived").value(1))
                .andExpect(jsonPath("$.successfullyImported").value(1));
    }

    @Test
    void testGetAllDeals_ReturnsOk() throws Exception {
        when(dealService.getAllDeals()).thenReturn(List.of(dealResponse));

        mockMvc.perform(get("/api/v1/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].dealUniqueId").value("DEAL-001"));
    }

    @Test
    void testGetDealByUniqueId_Exists_ReturnsOk() throws Exception {
        when(dealService.getDealByUniqueId("DEAL-001")).thenReturn(dealResponse);

        mockMvc.perform(get("/api/v1/deals/DEAL-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealUniqueId").value("DEAL-001"));
    }

    @Test
    void testGetDealByUniqueId_NotExists_ReturnsNotFound() throws Exception {
        when(dealService.getDealByUniqueId("DEAL-999")).thenReturn(null);

        mockMvc.perform(get("/api/v1/deals/DEAL-999"))
                .andExpect(status().isNotFound());
    }
}

