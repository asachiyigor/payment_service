package faang.school.payment_service.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentSendRequest(
        @NotNull
        long paymentNumber,

        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
        BigDecimal amount,

        @NotBlank(message = "CurrencyCode must not be blank")
        String currencyCode
) {
}