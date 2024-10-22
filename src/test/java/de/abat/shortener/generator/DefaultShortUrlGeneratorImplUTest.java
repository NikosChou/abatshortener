package de.abat.shortener.generator;

import de.abat.shortener.infrastructure.pool.DummyKeyPoolImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultShortUrlGeneratorImplUTest {

    private DummyKeyPoolImpl sut;

    @BeforeEach
    void init() {
        this.sut = new DummyKeyPoolImpl();
    }

    @Test
    void shouldGenerateRandomCodes() {
        String first = sut.pop();
        String second = sut.pop();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldGenerateCodesWithLength8() {
        assertThat(sut.pop()).hasSize(8);
    }

    @Test
    void shouldAlwaysReturnTheSameCodeWhenRemoveFromPool() {
        String actual = "12345678";
        assertThat(sut.pop(actual)).isEqualTo(actual);
        assertThat(sut.pop(actual)).isEqualTo(actual);
    }
}