package faang.school.payment_service.publisher;

import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClearingEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    private final ChannelTopic CLEARING_TOPIC;

    public void publishClearingEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(CLEARING_TOPIC.getTopic(), operation);
    }
}