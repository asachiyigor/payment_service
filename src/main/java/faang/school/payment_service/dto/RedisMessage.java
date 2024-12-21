package faang.school.payment_service.dto;

import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.Data;

@Data
public class RedisMessage {
    private String correlationId;
    private String type;
    private PaymentOperationDto payload;
    private String error;
}