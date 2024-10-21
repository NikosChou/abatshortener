package de.abat.shortener.generator;

import de.abat.shortener.infrastructure.generator.DefaultShortUrlGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultShortUrlGeneratorImplUTest {

    private DefaultShortUrlGeneratorImpl sut;

    @BeforeEach
    void init() {
        this.sut = new DefaultShortUrlGeneratorImpl();
    }

    @Test
    void shouldGenerateRandomCodes() {
        String first = sut.generate();
        String second = sut.generate();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldGenerateCodesWithLength8() {
        assertThat(sut.generate()).hasSize(8);
    }

    @Test
    void shouldAlwaysReturnTheSameCodeWhenRemoveFromPool() {
        String actual = "12345678";
        assertThat(sut.removeFromPool(actual)).isEqualTo(actual);
        assertThat(sut.removeFromPool(actual)).isEqualTo(actual);
    }
}