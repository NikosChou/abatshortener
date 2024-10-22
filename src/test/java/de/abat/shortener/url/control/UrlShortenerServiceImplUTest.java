package de.abat.shortener.url.control;

import de.abat.shortener.infrastructure.pool.KeyPool;
import de.abat.shortener.url.boundary.ShortenedUrlDto;
import de.abat.shortener.url.entity.ShortenedUrl;
import de.abat.shortener.url.entity.ShortenedUrlRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlShortenerServiceImplUTest {

    private UrlShortenerServiceImpl sut;
    private KeyPool shortUrlGenerator;
    private ShortenedUrlRepository shortenedUrlRepository;

    @BeforeEach
    void setup() {
        this.shortenedUrlRepository = mock(ShortenedUrlRepository.class);
        this.shortUrlGenerator = mock(KeyPool.class);
        this.sut = new UrlShortenerServiceImpl(shortUrlGenerator, shortenedUrlRepository);
    }

    @Test
    void whenShortCodeExistsShouldReturnTheUrl() {
        when(shortenedUrlRepository.findByShortCodeIgnoreCase(anyString()))
                .thenReturn(Optional.of(new ShortenedUrl("url", "short", null)));
        String actual = this.sut.getUrl("any").orElseThrow();

        assertThat(actual).isEqualTo("url");
    }

    @Test
    void whenShortCodeDoesntExistsShouldReturnEmpty() {
        when(shortenedUrlRepository.findByShortCodeIgnoreCase(anyString()))
                .thenReturn(Optional.empty());
        Optional<String> actual = this.sut.getUrl("any");

        assertThat(actual).isEmpty();
    }

    @MethodSource
    @ParameterizedTest
    void shouldCreateShortUrls(URL url, String customCode, Duration ttl, ShortenedUrlDto expected) {
        when(shortenedUrlRepository.save(any())).thenAnswer(a -> {
            ShortenedUrl entity = (ShortenedUrl) a.getArguments()[0];
            entity.setCreatedAt(ZonedDateTime.now());
            return entity;
        });

        when(this.shortUrlGenerator.pop(any())).thenAnswer(args -> Objects.requireNonNullElse(customCode, "random"));

        ShortenedUrlDto actual = this.sut.createShortUrl(url, customCode, ttl);

        assertThat(actual.url()).isEqualTo(expected.url());
        assertThat(actual.createdAt()).isCloseTo(expected.createdAt(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.code()).isEqualTo(expected.code());

        if (expected.validUntil() != null) {
            assertThat(actual.validUntil()).isCloseTo(expected.validUntil(), within(1, ChronoUnit.SECONDS));
        } else {
            assertThat(actual.validUntil()).isNull();
        }
    }

    @SneakyThrows
    private static Stream<Arguments> shouldCreateShortUrls() {
        return Stream.of(
                Arguments.of(new URI("https://google.com").toURL(), "abc", Duration.ofDays(1), new ShortenedUrlDto("https://google.com", "abc", ZonedDateTime.now().plusDays(1), ZonedDateTime.now())),
                Arguments.of(new URI("https://google.com").toURL(), null, Duration.ofDays(1), new ShortenedUrlDto("https://google.com", "random", ZonedDateTime.now().plusDays(1), ZonedDateTime.now())),
                Arguments.of(new URI("https://google.com").toURL(), null, null, new ShortenedUrlDto("https://google.com", "random", null, ZonedDateTime.now()))
        );
    }

    @MethodSource
    @ParameterizedTest
    void shouldValidateShortenedUrl(ShortenedUrl shortenedUrl, Boolean expected) {
        boolean actual = this.sut.isShortUrlValid(shortenedUrl);

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> shouldValidateShortenedUrl() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of(new ShortenedUrl(), true),
                Arguments.of(new ShortenedUrl("", "", ZonedDateTime.now().plusDays(1)), true),
                Arguments.of(new ShortenedUrl("", "", ZonedDateTime.now().minusDays(1)), false)
        );
    }
}