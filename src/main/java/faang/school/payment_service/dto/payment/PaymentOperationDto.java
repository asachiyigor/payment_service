package faang.school.payment_service.dto.payment;

import faang.school.payment_service.dto.PaymentStatus;
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
//    private Long Id;
//    private BigDecimal amount;
//    private String currency;
//    private long ownerId;
//    private long recipientId;
//    private PaymentOperationType operationType;
//    private PaymentStatus status;
//    private LocalDateTime clearScheduledAt;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;

    private Long id;
    private BigDecimal amount; //сумма
    private String currency; // валюта
    private long ownerId; //счет отправителя (проверку что такой счет существует)
    private long recipientId; //счет получателя (проверка что счет корректен)
    private PaymentOperationType operationType;
    private PaymentStatus status;
//    private LocalDateTime clearScheduledAt;
//    private LocalDateTime createdAt; //дата создания
//    private LocalDateTime updatedAt; //дата обновления
}