package de.abat.shortener.infrastructure.pool;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(value = "de.abat.key.pool.redis.enabled", havingValue = "false")
public class DummyKeyPoolImpl implements KeyPool {

    @Override
    public String pop() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String popCustom(String custom) {
        return custom;
    }
}
