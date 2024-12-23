package faang.school.payment_service.config.redis;

import faang.school.payment_service.listener.PaymentMessageEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.initiate_req_channel}")
    private String payment_service_initiate_request;

    @Value("${spring.data.redis.initiate_res_channel}")
    private String payment_service_initiate_response;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return redisTemplate;
    }

    @Bean
    ChannelTopic initiateReqChannelTopic() {
        return new ChannelTopic(payment_service_initiate_request);
    }

    @Bean
    ChannelTopic initiateResChannelTopic() {
        return new ChannelTopic(payment_service_initiate_response);
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            PaymentMessageEventListener paymentMessageEventListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(paymentMessageEventListener, initiateResChannelTopic());
        return container;
    }

    @Bean
    MessageListenerAdapter redisMessageListener(
            PaymentMessageEventListener paymentMessageEventListener) {
        return new MessageListenerAdapter(paymentMessageEventListener);
    }
}