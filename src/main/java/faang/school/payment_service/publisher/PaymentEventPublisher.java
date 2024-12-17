package faang.school.payment_service.publisher;

import faang.school.payment_service.dto.PaymentOperationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final RedisTemplate<String, PaymentOperationDto> redisTemplate;

    private static final String AUTHORIZATION_CHANNEL = "payment.authorization";
    private static final String CANCELLATION_CHANNEL = "payment.cancellation";
    private static final String CLEARING_CHANNEL = "payment.clearing";

    public void publishAuthorizationEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(AUTHORIZATION_CHANNEL, operation);
    }

    public void publishCancellationEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(CANCELLATION_CHANNEL, operation);
    }

    public void publishClearingEvent(PaymentOperationDto operation) {
        redisTemplate.convertAndSend(CLEARING_CHANNEL, operation);
    }
}