package faang.school.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOperationDto {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private String senderAccountId;
    private String recipientAccountId;
    private PaymentOperationType operationType;
    private PaymentStatus status;
    private LocalDateTime clearScheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}