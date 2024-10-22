package de.abat.shortener.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import({RedisAutoConfiguration.class})
@ConditionalOnProperty(value = "de.abat.key.pool.redis.enabled", havingValue = "true")
public class RedisConfiguration {

    @Bean
    ApplicationRunner initializeRedis(RedisKeyInitialization redisKeyInitialization) {
        return args -> redisKeyInitialization.initializeRedis();
    }

}
