package faang.school.payment_service.listener;

import faang.school.payment_service.dto.RedisMessage;
import faang.school.payment_service.publisher.PaymentMessageEventPublisher;
import faang.school.payment_service.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageEventListener implements MessageListener {
private final PaymentService paymentService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.debug("Received Redis message. Pattern: {}", new String(pattern));

            Jackson2JsonRedisSerializer<RedisMessage> serializer =
                    new Jackson2JsonRedisSerializer<>(RedisMessage.class);
            RedisMessage receivedMessage = serializer.deserialize(message.getBody());

            log.info("Deserialized message. Type: {}, CorrelationId: {}",
                    receivedMessage.getType(), receivedMessage.getCorrelationId());

            if ("RESPONSE".equals(receivedMessage.getType())) {
                processResponse(receivedMessage);
            } else {
                log.debug("Ignoring message of type: {}", receivedMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error processing Redis message: {}", e.getMessage(), e);
        }
    }

    private void processResponse(RedisMessage receivedMessage) {
        try {
            CompletableFuture<RedisMessage> future =
                    PaymentMessageEventPublisher.pendingRequests.get(receivedMessage.getCorrelationId());

            if (future != null) {
                future.complete(receivedMessage);
                log.debug("Completed future for correlationId: {}", receivedMessage.getCorrelationId());

                paymentService.updatePaymentOperation(receivedMessage.getPayload());
            } else {
                log.warn("No pending request found for correlationId: {}", receivedMessage.getCorrelationId());
            }
        } catch (Exception e) {
            log.error("Error processing response: {}", e.getMessage(), e);
        }
    }
}