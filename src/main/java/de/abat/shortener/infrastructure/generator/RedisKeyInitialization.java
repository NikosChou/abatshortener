package de.abat.shortener.infrastructure.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@ConditionalOnProperty(value = "de.abat.url.shortener.redis.enabled", havingValue = "true")
public class RedisKeyInitialization {
    private final String keySet;
    private final String base;
    private final int baseLength;
    private final int length;
    private final int step;
    private final StringRedisTemplate template;

    public RedisKeyInitialization(
            @Value("${de.abat.url.shortener.redis.key.set.name}") String keySet,
            @Value("${de.abat.url.shortener.redis.key.base}") String base,
            @Value("${de.abat.url.shortener.redis.key.length}") int length,
            @Value("${de.abat.url.shortener.redis.key.generator.step}") int step, StringRedisTemplate template) {
        this.base = base;
        this.length = length;
        this.step = step;
        this.keySet = keySet;
        this.baseLength = base.length();
        this.template = template;
    }

    public void initializeRedis() {
        Long size = template.opsForSet().size(keySet);
        if (size != null && size == 0) {
            log.trace("Redis Initializer started");
            long totalCombinations = generateAllBaseCombinations();
            log.trace("Total combinations: " + totalCombinations);
        }
    }

    private long generateAllBaseCombinations() {
        int totalCombinations = (int) Math.pow(base.length(), length);

        createRangeStreams(totalCombinations)
                .parallel()
                .forEach(range -> {
                    String[] array = new String[range.endEx() - range.startIn()];
                    for (int i = range.startIn(); i < range.endEx(); i++) {
                        array[i % step] = toBaseString(i);
                    }
                    template.opsForSet().add(keySet, array);
                });


        return totalCombinations;
    }

    private Stream<Range> createRangeStreams(int totalCombinations) {
        return IntStream.rangeClosed(0, totalCombinations / step)
                .mapToObj(i -> new Range(i * step, Math.min((i + 1) * step, totalCombinations)));
    }

    private String toBaseString(int number) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int remainder = number % baseLength;
            sb.append(base.charAt(remainder));
            number /= baseLength;
        }

        return sb.reverse().toString();
    }
}
