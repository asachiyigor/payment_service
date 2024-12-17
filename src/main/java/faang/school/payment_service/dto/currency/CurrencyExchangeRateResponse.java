package faang.school.payment_service.dto.currency;

import java.util.Map;

public record CurrencyExchangeRateResponse(
        Map<String, Double> rates
) {}