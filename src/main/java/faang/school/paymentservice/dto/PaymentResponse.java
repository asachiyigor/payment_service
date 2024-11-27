package faang.school.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        PaymentStatus status,
        int verificationCode,
        long paymentNumber,
        BigDecimal amount,
        String currencyCode,
        String convertAmountWithCommission,
        Currency convertCurrencyCode,
        String message
) {
}
