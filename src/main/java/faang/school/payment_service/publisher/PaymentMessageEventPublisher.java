package faang.school.payment_service.publisher;

import faang.school.payment_service.dto.RedisMessage;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentMessageEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic initiateReqChannelTopic;
    public static final ConcurrentHashMap<String, CompletableFuture<RedisMessage>> pendingRequests = new ConcurrentHashMap<>();

    public PaymentOperationDto sendAndReceive(PaymentOperationDto payment, long timeout, TimeUnit unit) {
        String correlationId = UUID.randomUUID().toString();
        RedisMessage request = new RedisMessage();
        request.setCorrelationId(correlationId);
        request.setType("REQUEST");
        request.setPayload(payment);

        CompletableFuture<RedisMessage> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        try {
            redisTemplate.convertAndSend(initiateReqChannelTopic.getTopic(), request);
            RedisMessage response = future.get(timeout, unit);
            if (response.getError() != null) {
                throw new RuntimeException(response.getError());
            }
            return response.getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get response", e);
        } finally {
            pendingRequests.remove(correlationId);
        }
    }
}