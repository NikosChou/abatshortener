package de.abat.shortener.generator;

import de.abat.shortener.infrastructure.generator.RedisKeyInitialization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedisKeyInitializationUTest {

    private RedisKeyInitialization sut;
    private final String base = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private final String keySet = "set-name";
    private final int length = 2;
    private final int step = 100;
    private StringRedisTemplate template;
    private SetOperations<String, String> operations;

    @BeforeEach
    void setup() {
        template = mock(StringRedisTemplate.class);
        operations = mock(SetOperations.class);
        when(template.opsForSet()).thenReturn(operations);
        this.sut = new RedisKeyInitialization(keySet, base, length, step, template);
    }

    @Test
    void shouldInitializeSet() {
        when(operations.size(eq(keySet))).thenReturn(0L);
        this.sut.initializeRedis();
        int totalCombinations = (int) Math.pow(base.length(), length);
        int wantedNumberOfInvocations = Math.ceilDiv(totalCombinations, step);
        verify(operations, times(wantedNumberOfInvocations)).add(eq(keySet), any(String[].class));
    }

    @Test
    void shouldNotInitializeSameSetIfAlreadyExists() {
        when(operations.size(eq(keySet))).thenReturn(10L);
        this.sut.initializeRedis();
        verify(operations, never()).add(anyString(), any());
    }

    @Test
    void whenInitializeBase33WithLength2ShouldGenerate1089Codes() {
        when(operations.size(eq(keySet))).thenReturn(0L);
        List<String> accumulate = new ArrayList<>(1089);
        when(operations.add(eq(keySet), any(String[].class))).thenAnswer(args -> {
            String[] arg = (String[]) args.getRawArguments()[1];
            accumulate.addAll(Arrays.asList(arg));
            return (long) arg.length;
        });
        this.sut.initializeRedis();

        assertThat(accumulate).hasSize(1089)
                .doesNotHaveDuplicates();
    }
}