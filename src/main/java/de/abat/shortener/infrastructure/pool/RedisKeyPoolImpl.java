package de.abat.shortener.infrastructure.pool;

import de.abat.shortener.infrastructure.exceptions.KeyNotFoundInPoolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@ConditionalOnProperty(value = "de.abat.key.pool.redis.enabled", havingValue = "true")
public class RedisKeyPoolImpl implements KeyPool {
    private final StringRedisTemplate stringRedisTemplate;
    private final String keySet;
    private final int keyLength;


    public RedisKeyPoolImpl(
            StringRedisTemplate stringRedisTemplate,
            @Value("${de.abat.key.pool.redis.name}") String keySet,
            @Value("${de.abat.key.pool.key.length}") int keyLength) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.keySet = keySet;
        this.keyLength = keyLength;
    }

    @Override
    public String pop() {
        return stringRedisTemplate.opsForSet().pop(keySet);
    }

    @Override
    public String popCustom(String custom) {
        if (custom.length() == keyLength) {
            log.trace("removing from pool {}", custom);
            Long removed = stringRedisTemplate.opsForSet().remove(keySet, custom.toUpperCase());
            log.trace("Removed {}", removed);
            if (Objects.equals(removed, 0L)) {
                throw new KeyNotFoundInPoolException(String.format("Key %s doesn't exist in pool", custom));
            }
        }
        return custom;
    }
}
