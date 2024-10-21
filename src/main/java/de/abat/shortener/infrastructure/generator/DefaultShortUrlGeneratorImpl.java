package de.abat.shortener.infrastructure.generator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(value = "de.abat.url.shortener.redis.enabled", havingValue = "false")
public class DefaultShortUrlGeneratorImpl implements ShortUrlGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String removeFromPool(String custom) {
        return custom;
    }
}
