package faang.school.payment_service.publisher;

import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic AUTHORIZATION_TOPIC;

    public void publishAuthorizationEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(AUTHORIZATION_TOPIC.getTopic(), operation);
    }
}