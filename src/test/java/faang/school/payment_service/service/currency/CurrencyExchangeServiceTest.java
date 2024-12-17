package faang.school.payment_service.service.currency;

import faang.school.payment_service.client.exchange.OpenExchangeRatesClient;
import faang.school.payment_service.dto.currency.CurrencyExchangeRateResponse;
import faang.school.payment_service.dto.currency.CurrencyPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceTest {
    private CurrencyExchangeRateResponse mockResponse;

    @Mock
    private OpenExchangeRatesClient exchangeRatesClient;

    @InjectMocks
    private CurrencyExchangeService currencyExchangeService;

    @BeforeEach
    void setUp() {
        mockResponse = new CurrencyExchangeRateResponse(Map.of("EUR", 0.95));

        try {
            var appIdField = CurrencyExchangeService.class.getDeclaredField("appId");
            appIdField.setAccessible(true);
            appIdField.set(currencyExchangeService, "test-app-id");
            var commissionField = CurrencyExchangeService.class.getDeclaredField("commission");
            commissionField.setAccessible(true);
            commissionField.set(currencyExchangeService, BigDecimal.valueOf(0.01));
        } catch (Exception e) {
            throw new RuntimeException("Could not set up test fields", e);
        }
    }

    @Test
    @DisplayName("When payment is in USD, should return original amount without conversion")
    public void testConvertCurrency_UsdPayment_ReturnsOriginalAmount() {
        CurrencyPaymentRequest usdPayment = createPaymentRequest("USD", new BigDecimal("100.00"));
        BigDecimal result = currencyExchangeService.convertCurrency(usdPayment);
        assertEquals(new BigDecimal("100.00"), result);
        verifyNoInteractions(exchangeRatesClient);
    }


    @Test
    @DisplayName("When payment is in EUR, should convert to USD and apply commission")
    void testConvertCurrency_EUR() {
        when(exchangeRatesClient.getLatestRates("test-app-id"))
                .thenReturn(mockResponse);
        CurrencyPaymentRequest request = createPaymentRequest("EUR", new BigDecimal("100.00"));
        BigDecimal convertedAmount = currencyExchangeService.convertCurrency(request);
        BigDecimal UsdAmount = request.amount()
                .divide(BigDecimal.valueOf(0.95), 2, RoundingMode.HALF_UP);
        BigDecimal commissionAmount = UsdAmount.multiply(BigDecimal.valueOf(0.01));
        BigDecimal expectedAmount = UsdAmount.add(commissionAmount);
        assertEquals(expectedAmount, convertedAmount,
                "Converted amount should match expected calculation");
    }

    @Test
    @DisplayName("When payment currency is unsupported, should throw an exception")
    public void testConvertCurrency_UnsupportedCurrency_ThrowsException() {
        CurrencyPaymentRequest unsupportedPayment = createPaymentRequest("XYZ", new BigDecimal("100.00"));
        when(exchangeRatesClient.getLatestRates(anyString()))
                .thenReturn(mockResponse);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyExchangeService.convertCurrency(unsupportedPayment)
        );
        assertTrue(exception.getMessage().contains("Unable to process currency conversion. Please make the payment in USD."));
    }

    @Test
    @DisplayName("When external exchange rate API fails, should throw an exception")
    public void testConvertCurrency_ExternalApiFailure_ThrowsException() {
        CurrencyPaymentRequest eurPayment = createPaymentRequest("EUR", new BigDecimal("100.00"));
        when(exchangeRatesClient.getLatestRates(anyString()))
                .thenThrow(new RuntimeException("API connection error"));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyExchangeService.convertCurrency(eurPayment)
        );
        assertEquals(
                "Unable to process currency conversion. Please make the payment in USD.",
                exception.getMessage()
        );
    }

    private CurrencyPaymentRequest createPaymentRequest(String currencyCode, BigDecimal amount) {
        return new CurrencyPaymentRequest(
                1L,
                amount,
                String.valueOf(currencyCode)
        );
    }
}