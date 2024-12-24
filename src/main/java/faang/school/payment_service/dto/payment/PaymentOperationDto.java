package faang.school.payment_service.dto.payment;

import faang.school.payment_service.dto.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOperationDto {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private long ownerAccId;
    private long recipientAccId;
    private PaymentOperationType operationType;
    private PaymentStatus status;
    private String clearScheduledAt;
    private String createdAt;
    private String updatedAt;
}