package com.bloomberg.fxdeals.service;

import com.bloomberg.fxdeals.dto.BulkDealResponse;
import com.bloomberg.fxdeals.dto.DealRequest;
import com.bloomberg.fxdeals.dto.DealResponse;
import com.bloomberg.fxdeals.model.FxDeal;
import com.bloomberg.fxdeals.repository.FxDealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FxDealService
 */
@ExtendWith(MockitoExtension.class)
class FxDealServiceTest {

    @Mock
    private FxDealRepository dealRepository;

    @Mock
    private DealValidationService validationService;

    @InjectMocks
    private FxDealService dealService;

    private DealRequest validDealRequest;
    private FxDeal savedDeal;

    @BeforeEach
    void setUp() {
        validDealRequest = new DealRequest();
        validDealRequest.setDealUniqueId("DEAL-001");
        validDealRequest.setFromCurrencyIsoCode("USD");
        validDealRequest.setToCurrencyIsoCode("EUR");
        validDealRequest.setDealTimestamp(LocalDateTime.now().minusHours(1));
        validDealRequest.setDealAmount(new BigDecimal("1000.50"));

        savedDeal = FxDeal.builder()
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
    void testImportDeal_ValidDeal_Success() {
        when(validationService.validateDeal(any(DealRequest.class))).thenReturn(List.of());
        when(dealRepository.existsByDealUniqueId("DEAL-001")).thenReturn(false);
        when(dealRepository.save(any(FxDeal.class))).thenReturn(savedDeal);

        DealResponse response = dealService.importDeal(validDealRequest);

        assertThat(response).isNotNull();
        assertThat(response.getDealUniqueId()).isEqualTo("DEAL-001");
        assertThat(response.getFromCurrencyIsoCode()).isEqualTo("USD");
        assertThat(response.getToCurrencyIsoCode()).isEqualTo("EUR");

        verify(validationService).validateDeal(validDealRequest);
        verify(dealRepository).existsByDealUniqueId("DEAL-001");
        verify(dealRepository).save(any(FxDeal.class));
    }

    @Test
    void testImportDeal_ValidationFails_ThrowsException() {
        when(validationService.validateDeal(any(DealRequest.class)))
            .thenReturn(List.of("Invalid currency code"));

        assertThatThrownBy(() -> dealService.importDeal(validDealRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Validation failed");

        verify(validationService).validateDeal(validDealRequest);
        verify(dealRepository, never()).save(any());
    }

    @Test
    void testImportDeal_DuplicateDeal_ThrowsException() {
        when(validationService.validateDeal(any(DealRequest.class))).thenReturn(List.of());
        when(dealRepository.existsByDealUniqueId("DEAL-001")).thenReturn(true);

        assertThatThrownBy(() -> dealService.importDeal(validDealRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already exists");

        verify(validationService).validateDeal(validDealRequest);
        verify(dealRepository).existsByDealUniqueId("DEAL-001");
        verify(dealRepository, never()).save(any());
    }

    @Test
    void testImportDealsBulk_AllValid_Success() {
        DealRequest deal1 = createValidDeal("DEAL-001");
        DealRequest deal2 = createValidDeal("DEAL-002");
        List<DealRequest> deals = List.of(deal1, deal2);

        when(validationService.validateDeal(any(DealRequest.class))).thenReturn(List.of());
        when(dealRepository.existsByDealUniqueId(anyString())).thenReturn(false);
        when(dealRepository.save(any(FxDeal.class))).thenAnswer(invocation -> {
            FxDeal deal = invocation.getArgument(0);
            return FxDeal.builder()
                .id(deal.getDealUniqueId().equals("DEAL-001") ? 1L : 2L)
                .dealUniqueId(deal.getDealUniqueId())
                .fromCurrencyIsoCode(deal.getFromCurrencyIsoCode())
                .toCurrencyIsoCode(deal.getToCurrencyIsoCode())
                .dealTimestamp(deal.getDealTimestamp())
                .dealAmount(deal.getDealAmount())
                .createdAt(LocalDateTime.now())
                .build();
        });

        BulkDealResponse response = dealService.importDealsBulk(deals);

        assertThat(response.getTotalReceived()).isEqualTo(2);
        assertThat(response.getSuccessfullyImported()).isEqualTo(2);
        assertThat(response.getSkippedDuplicates()).isEqualTo(0);
        assertThat(response.getFailed()).isEqualTo(0);
        assertThat(response.getImportedDeals()).hasSize(2);
    }

    @Test
    void testImportDealsBulk_WithDuplicates_SkipsDuplicates() {
        DealRequest deal1 = createValidDeal("DEAL-001");
        DealRequest deal2 = createValidDeal("DEAL-002");
        List<DealRequest> deals = List.of(deal1, deal2);

        when(validationService.validateDeal(any(DealRequest.class))).thenReturn(List.of());
        when(dealRepository.existsByDealUniqueId("DEAL-001")).thenReturn(false);
        when(dealRepository.existsByDealUniqueId("DEAL-002")).thenReturn(true); // Duplicate
        when(dealRepository.save(any(FxDeal.class))).thenReturn(savedDeal);

        BulkDealResponse response = dealService.importDealsBulk(deals);

        assertThat(response.getTotalReceived()).isEqualTo(2);
        assertThat(response.getSuccessfullyImported()).isEqualTo(1);
        assertThat(response.getSkippedDuplicates()).isEqualTo(1);
        assertThat(response.getFailed()).isEqualTo(0);
    }

    @Test
    void testImportDealsBulk_WithValidationErrors_FailsInvalidDeals() {
        DealRequest deal1 = createValidDeal("DEAL-001");
        DealRequest deal2 = createValidDeal("DEAL-002");
        List<DealRequest> deals = List.of(deal1, deal2);

        when(validationService.validateDeal(deal1)).thenReturn(List.of());
        when(validationService.validateDeal(deal2)).thenReturn(List.of("Invalid currency"));
        when(dealRepository.existsByDealUniqueId("DEAL-001")).thenReturn(false);
        when(dealRepository.save(any(FxDeal.class))).thenReturn(savedDeal);

        BulkDealResponse response = dealService.importDealsBulk(deals);

        assertThat(response.getTotalReceived()).isEqualTo(2);
        assertThat(response.getSuccessfullyImported()).isEqualTo(1);
        assertThat(response.getFailed()).isEqualTo(1);
        assertThat(response.getErrors()).isNotEmpty();
    }

    @Test
    void testImportDealsBulk_DuplicateInBatch_DetectsAndFails() {
        DealRequest deal1 = createValidDeal("DEAL-001");
        DealRequest deal2 = createValidDeal("DEAL-001"); // Duplicate in batch
        List<DealRequest> deals = List.of(deal1, deal2);

        when(validationService.validateDeal(any(DealRequest.class))).thenReturn(List.of());
        when(dealRepository.existsByDealUniqueId("DEAL-001")).thenReturn(false);
        when(dealRepository.save(any(FxDeal.class))).thenReturn(savedDeal);

        BulkDealResponse response = dealService.importDealsBulk(deals);

        assertThat(response.getTotalReceived()).isEqualTo(2);
        assertThat(response.getSuccessfullyImported()).isEqualTo(1);
        assertThat(response.getFailed()).isEqualTo(1);
        assertThat(response.getErrors()).anyMatch(e -> e.contains("Duplicate deal ID in the same batch"));
    }

    @Test
    void testGetAllDeals_ReturnsAllDeals() {
        FxDeal deal2 = FxDeal.builder()
            .id(2L)
            .dealUniqueId("DEAL-002")
            .fromCurrencyIsoCode("GBP")
            .toCurrencyIsoCode("JPY")
            .dealTimestamp(LocalDateTime.now())
            .dealAmount(new BigDecimal("2000.00"))
            .createdAt(LocalDateTime.now())
            .build();

        when(dealRepository.findAll()).thenReturn(List.of(savedDeal, deal2));

        List<DealResponse> responses = dealService.getAllDeals();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getDealUniqueId()).isEqualTo("DEAL-001");
        assertThat(responses.get(1).getDealUniqueId()).isEqualTo("DEAL-002");
    }

    @Test
    void testGetDealByUniqueId_Exists_ReturnsDeal() {
        when(dealRepository.findByDealUniqueId("DEAL-001")).thenReturn(Optional.of(savedDeal));

        DealResponse response = dealService.getDealByUniqueId("DEAL-001");

        assertThat(response).isNotNull();
        assertThat(response.getDealUniqueId()).isEqualTo("DEAL-001");
    }

    @Test
    void testGetDealByUniqueId_NotExists_ReturnsNull() {
        when(dealRepository.findByDealUniqueId("DEAL-999")).thenReturn(Optional.empty());

        DealResponse response = dealService.getDealByUniqueId("DEAL-999");

        assertThat(response).isNull();
    }

    private DealRequest createValidDeal(String dealId) {
        DealRequest deal = new DealRequest();
        deal.setDealUniqueId(dealId);
        deal.setFromCurrencyIsoCode("USD");
        deal.setToCurrencyIsoCode("EUR");
        deal.setDealTimestamp(LocalDateTime.now().minusHours(1));
        deal.setDealAmount(new BigDecimal("1000.50"));
        return deal;
    }
}

