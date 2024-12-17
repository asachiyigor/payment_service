package faang.school.payment_service.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentInitiateRequest {
    private String senderAccountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime clearScheduledAt;
}