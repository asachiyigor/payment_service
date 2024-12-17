package faang.school.payment_service.dto.currency;

import faang.school.payment_service.dto.PaymentStatus;

import java.math.BigDecimal;

public record CurrencyPaymentResponse(
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
