package com.campus.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 序列化：key 用 String（redis-cli 里可读），value 用 JSON。
 * Spring Data Redis 4.x（对应 Boot 4.1）把 JSON 主库切到了 Jackson 3，
 * 旧类 GenericJackson2JsonRedisSerializer 已 deprecated for removal。
 *
 * 注意：4.x 的 GenericJacksonJsonRedisSerializer 不再提供无参构造，必须走 builder()。
 * enableUnsafeDefaultTyping() 会往 JSON 里写类型信息（@class），
 * 这样反序列化才能还原成 ProductDetailVO 等具体类型而不是 LinkedHashMap；
 * 教学项目用它是安全的，生产环境若 Redis 可能被外部写入，应改为 allow-list 的 enableDefaultTyping(validator)。
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer str = new StringRedisSerializer();
        GenericJacksonJsonRedisSerializer json = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();

        template.setKeySerializer(str);
        template.setHashKeySerializer(str);
        template.setValueSerializer(json);
        template.setHashValueSerializer(json);
        template.afterPropertiesSet();
        return template;
    }
}
