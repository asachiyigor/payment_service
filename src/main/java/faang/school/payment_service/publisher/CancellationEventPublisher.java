package faang.school.payment_service.publisher;

import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancellationEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic CANCELLATION_TOPIC;

    public void publishCancellationEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(CANCELLATION_TOPIC.getTopic(), operation);
    }
}