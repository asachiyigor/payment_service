package faang.school.payment_service.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.payment_service.dto.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentInitiateRequest {
    private long ownerId;
    private long recipientId;
    private BigDecimal amount;
    private String currency;
//    private LocalDateTime clearScheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PaymentOperationType INITIATE;
    private PaymentStatus PENDING;
}