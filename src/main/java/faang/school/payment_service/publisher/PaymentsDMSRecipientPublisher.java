package faang.school.payment_service.publisher;

import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentsDMSRecipientPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    private final ChannelTopic payments_DMS_recipient;

    public void publishPaymentsDMSRecipientEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(payments_DMS_recipient.getTopic(), operation);
    }
}