package de.abat.shortener.infrastructure.generator;

import de.abat.shortener.infrastructure.exceptions.ShortGeneratorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@ConditionalOnProperty(value = "de.abat.url.shortener.redis.enabled", havingValue = "true")
public class RedisShortUrlGeneratorImpl implements ShortUrlGenerator {
    private final StringRedisTemplate stringRedisTemplate;
    private final String keySet;
    private final int keyLength;


    public RedisShortUrlGeneratorImpl(
            StringRedisTemplate stringRedisTemplate,
            @Value("${de.abat.url.shortener.redis.key.set.name}") String keySet,
            @Value("${de.abat.url.shortener.redis.key.length}") int keyLength) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.keySet = keySet;
        this.keyLength = keyLength;
    }

    @Override
    public String generate() {
        return stringRedisTemplate.opsForSet().pop(keySet);
    }

    @Override
    public String removeFromPool(String custom) {
        if (custom.length() == keyLength) {
            log.trace("removing from pool {}", custom);
            Long removed = stringRedisTemplate.opsForSet().remove(keySet, custom.toUpperCase());
            log.trace("Removed {}", removed);
            if (Objects.equals(removed, 0L)) {
                throw new ShortGeneratorException(String.format("Short code %s doesn't exist in pool", custom));
            }
        }
        return custom;
    }
}
