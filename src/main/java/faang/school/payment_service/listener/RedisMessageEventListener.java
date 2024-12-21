package faang.school.payment_service.listener;

import faang.school.payment_service.dto.RedisMessage;
import faang.school.payment_service.redis.RedisMessageBroker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class RedisMessageEventListener implements MessageListener {
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
                CompletableFuture<RedisMessage> future =
                        RedisMessageBroker.pendingRequests.get(receivedMessage.getCorrelationId());

                if (future != null) {
                    future.complete(receivedMessage);
                    log.debug("Completed future for correlationId: {}", receivedMessage.getCorrelationId());
                } else {
                    log.warn("No pending request found for correlationId: {}", receivedMessage.getCorrelationId());
                }
            } else {
                log.debug("Ignoring message of type: {}", receivedMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error processing Redis message: {}", e.getMessage(), e);
        }
    }
}