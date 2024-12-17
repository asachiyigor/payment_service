package faang.school.payment_service.config.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.channels.AUTHORIZATION-CHANNEL.name}")
    private String AUTHORIZATION_CHANNEL;

    @Value("${spring.data.redis.channels.CANCELLATION-CHANNEL.name}")
    private String CANCELLATION_CHANNEL;

    @Value("${spring.data.redis.channels.CLEARING-CHANNEL.name}")
    private String CLEARING_CHANNEL;

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
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public ChannelTopic AUTHORIZATION() {
        return new ChannelTopic(AUTHORIZATION_CHANNEL);
    }

    @Bean
    public ChannelTopic CANCELLATION() {
        return new ChannelTopic(CANCELLATION_CHANNEL);
    }

    @Bean
    public ChannelTopic CLEARING() {
        return new ChannelTopic(CLEARING_CHANNEL);
    }
}