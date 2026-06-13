package com.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {

    /**
     * 캐시 값의 타입 정보를 보존하는 JSON 직렬화와 10분 TTL을 적용한다.
     *
     * @param objectMapper 애플리케이션 공통 Jackson 설정
     * @return Redis 캐시에 적용할 기본 설정
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {

        ObjectMapper objectMapperCopy = objectMapper.copy();
        objectMapperCopy.registerModule(new JavaTimeModule());

        GenericJackson2JsonRedisSerializer serializer = GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(objectMapperCopy)
                .defaultTyping(true)
                .build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
        );
    }
}
