package faang.school.payment_service.dto;

import java.math.BigDecimal;

public record PaymentSendResponse(
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
