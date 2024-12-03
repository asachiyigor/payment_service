package faang.school.paymentservice.dto;

import java.util.Map;

public record ExchangeRateResponse(
        Map<String, Double> rates
) {}