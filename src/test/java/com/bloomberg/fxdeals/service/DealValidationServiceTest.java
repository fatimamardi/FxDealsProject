package com.bloomberg.fxdeals.service;

import com.bloomberg.fxdeals.dto.DealRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DealValidationService
 */
@ExtendWith(MockitoExtension.class)
class DealValidationServiceTest {

    @InjectMocks
    private DealValidationService validationService;

    private DealRequest validDealRequest;

    @BeforeEach
    void setUp() {
        validDealRequest = new DealRequest();
        validDealRequest.setDealUniqueId("DEAL-001");
        validDealRequest.setFromCurrencyIsoCode("USD");
        validDealRequest.setToCurrencyIsoCode("EUR");
        validDealRequest.setDealTimestamp(LocalDateTime.now().minusHours(1));
        validDealRequest.setDealAmount(new BigDecimal("1000.50"));
    }

    @Test
    void testValidateDeal_ValidDeal_ReturnsNoErrors() {
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isEmpty();
    }

    @Test
    void testValidateDeal_NullDeal_ReturnsError() {
        List<String> errors = validationService.validateDeal(null);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("cannot be null");
    }

    @Test
    void testValidateDeal_MissingDealUniqueId_ReturnsError() {
        validDealRequest.setDealUniqueId(null);
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Deal Unique Id is required"));
    }

    @Test
    void testValidateDeal_EmptyDealUniqueId_ReturnsError() {
        validDealRequest.setDealUniqueId("");
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Deal Unique Id is required"));
    }

    @Test
    void testValidateDeal_DealUniqueIdTooLong_ReturnsError() {
        validDealRequest.setDealUniqueId("A".repeat(101));
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("must not exceed 100 characters"));
    }

    @Test
    void testValidateDeal_MissingFromCurrency_ReturnsError() {
        validDealRequest.setFromCurrencyIsoCode(null);
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("From Currency"));
    }

    @Test
    void testValidateDeal_InvalidCurrencyCodeLength_ReturnsError() {
        validDealRequest.setFromCurrencyIsoCode("US");
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("exactly 3 characters"));
    }

    @Test
    void testValidateDeal_InvalidCurrencyCodeFormat_ReturnsError() {
        validDealRequest.setFromCurrencyIsoCode("123");
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("uppercase letters"));
    }

    @Test
    void testValidateDeal_SameFromAndToCurrency_ReturnsError() {
        validDealRequest.setFromCurrencyIsoCode("USD");
        validDealRequest.setToCurrencyIsoCode("USD");
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("must be different"));
    }

    @Test
    void testValidateDeal_MissingTimestamp_ReturnsError() {
        validDealRequest.setDealTimestamp(null);
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("timestamp is required"));
    }

    @Test
    void testValidateDeal_FutureTimestamp_ReturnsError() {
        validDealRequest.setDealTimestamp(LocalDateTime.now().plusHours(1));
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("cannot be in the future"));
    }

    @Test
    void testValidateDeal_MissingAmount_ReturnsError() {
        validDealRequest.setDealAmount(null);
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("amount is required"));
    }

    @Test
    void testValidateDeal_ZeroAmount_ReturnsError() {
        validDealRequest.setDealAmount(BigDecimal.ZERO);
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("must be greater than 0"));
    }

    @Test
    void testValidateDeal_NegativeAmount_ReturnsError() {
        validDealRequest.setDealAmount(new BigDecimal("-100"));
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("must be greater than 0"));
    }

    @Test
    void testValidateDeal_TooManyDecimalPlaces_ReturnsError() {
        validDealRequest.setDealAmount(new BigDecimal("1000.12345"));
        List<String> errors = validationService.validateDeal(validDealRequest);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("more than 4 decimal places"));
    }

    @Test
    void testValidateDeal_ValidLowercaseCurrency_ConvertsToUppercase() {
        validDealRequest.setFromCurrencyIsoCode("usd");
        validDealRequest.setToCurrencyIsoCode("eur");
        List<String> errors = validationService.validateDeal(validDealRequest);
        // Validation should pass (format is checked, case conversion happens in service)
        assertThat(errors).isEmpty();
    }

    @Test
    void testValidateDeals_MultipleValidDeals_ReturnsNoErrors() {
        DealRequest deal1 = createValidDeal("DEAL-001");
        DealRequest deal2 = createValidDeal("DEAL-002");
        List<DealRequest> deals = List.of(deal1, deal2);

        List<String> errors = validationService.validateDeals(deals);
        assertThat(errors).isEmpty();
    }

    @Test
    void testValidateDeals_MultipleDealsWithErrors_ReturnsAllErrors() {
        DealRequest deal1 = createValidDeal("DEAL-001");
        DealRequest deal2 = new DealRequest(); // Invalid
        DealRequest deal3 = createValidDeal("DEAL-003");
        deal3.setDealAmount(BigDecimal.ZERO); // Invalid amount

        List<DealRequest> deals = List.of(deal1, deal2, deal3);
        List<String> errors = validationService.validateDeals(deals);

        assertThat(errors).isNotEmpty();
        assertThat(errors.size()).isGreaterThan(1);
    }

    @Test
    void testValidateDeals_NullList_ReturnsError() {
        List<String> errors = validationService.validateDeals(null);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("cannot be null or empty");
    }

    @Test
    void testValidateDeals_EmptyList_ReturnsError() {
        List<String> errors = validationService.validateDeals(List.of());
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("cannot be null or empty");
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

