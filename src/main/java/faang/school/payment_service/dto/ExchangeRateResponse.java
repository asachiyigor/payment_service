package faang.school.payment_service.dto;

import java.util.Map;

public record ExchangeRateResponse(
        Map<String, Double> rates
) {}