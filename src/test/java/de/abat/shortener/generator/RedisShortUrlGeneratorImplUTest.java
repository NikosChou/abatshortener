package de.abat.shortener.generator;

import de.abat.shortener.infrastructure.exceptions.KeyNotFoundInPoolException;
import de.abat.shortener.infrastructure.pool.RedisKeyPoolImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedisShortUrlGeneratorImplUTest {

    private RedisKeyPoolImpl sut;
    private SetOperations<String, String> mockOps;
    private final String keySet = "set-name";

    @BeforeEach
    void setup() {
        StringRedisTemplate mock = mock(StringRedisTemplate.class);
        mockOps = mock(SetOperations.class);
        when(mock.opsForSet()).thenReturn(mockOps);
        int keyLength = 5;
        when(mockOps.pop(anyString())).thenAnswer(args -> UUID.randomUUID().toString().substring(0, keyLength));
        when(mockOps.remove(eq(keySet), anyString())).thenReturn(1L);
        this.sut = new RedisKeyPoolImpl(mock, keySet, keyLength);
    }

    @Test
    void shouldPopRandomCodeWithLengthOf5() {
        assertThat(this.sut.pop()).hasSize(5);
        verify(mockOps, times(1)).pop(anyString());
    }

    @Test
    void shouldPopRandomCodes() {
        assertThat(this.sut.pop()).isNotEqualTo(this.sut.pop());
        verify(mockOps, times(2)).pop(anyString());
    }

    @Test
    void whenRemovedFromPoolFirstTimeShouldReturn() {
        assertThat(this.sut.pop("abcde")).isEqualTo("abcde");
        verify(mockOps).remove(anyString(), anyString());
    }

    @Test
    void whenRemovedFromPoolTimeNotExistsInPoolShouldThrownException() {
        when(mockOps.remove(eq(keySet), anyString())).thenReturn(0L);
        assertThatThrownBy(() -> this.sut.pop("abcde")).isInstanceOf(KeyNotFoundInPoolException.class)
                .hasMessage(String.format("Key %s doesn't exist in pool", "abcde"));
    }

    @Test
    void whenRemovedFromPoolCodeWithLessOrMoreCharsShouldReturnTheSame() {
        assertThat(this.sut.pop("abcdefg")).isEqualTo("abcdefg");
        assertThat(this.sut.pop("abcdefg")).isEqualTo("abcdefg");

        assertThat(this.sut.pop("abc")).isEqualTo("abc");
        assertThat(this.sut.pop("abc")).isEqualTo("abc");

        verify(mockOps, never()).remove(anyString(), anyString());
        verify(mockOps, never()).pop(anyString());
    }

}