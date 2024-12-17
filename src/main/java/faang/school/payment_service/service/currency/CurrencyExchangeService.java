package faang.school.payment_service.service.currency;

import faang.school.payment_service.client.exchange.OpenExchangeRatesClient;
import faang.school.payment_service.dto.currency.CurrencyExchangeRateResponse;
import faang.school.payment_service.dto.currency.CurrencyPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyExchangeService {
    private final OpenExchangeRatesClient exchangeRatesClient;

    @Value("${currency.exchange.appId}")
    private String appId;

    @Value("${currency.exchange.commission}")
    private BigDecimal commission;

    public BigDecimal convertCurrency(CurrencyPaymentRequest request) {
        if (request.currencyCode().equals("USD")) {
            return request.amount();
        }

        try {
            CurrencyExchangeRateResponse rateResponse = exchangeRatesClient.getLatestRates(appId);
            Map<String, Double> rates = rateResponse.rates();

            if (!rates.containsKey(request.currencyCode())) {
                throw new IllegalArgumentException(
                        "Unsupported currency. Please send payment in USD. " +
                                "Currency " + request.currencyCode() + " is not supported."
                );
            }

            double currencyRate = rates.get(request.currencyCode());
            BigDecimal usdAmount = request.amount()
                    .divide(BigDecimal.valueOf(currencyRate), 2, RoundingMode.HALF_UP);
            BigDecimal commissionAmount = usdAmount.multiply(commission);
            return usdAmount.add(commissionAmount);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unable to process currency conversion. Please make the payment in USD.", e);
        }
    }
}