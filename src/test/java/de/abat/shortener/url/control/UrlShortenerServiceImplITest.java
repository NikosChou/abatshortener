package de.abat.shortener.url.control;

import de.abat.shortener.testcontainers.UrlShortenerServiceImplITestConfiguration;
import de.abat.shortener.url.boundary.ShortenedUrlDto;
import de.abat.shortener.url.boundary.UrlShortenerService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerServiceImplITest extends UrlShortenerServiceImplITestConfiguration {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Test
    @SneakyThrows
    void when2ParallelThreadsRequestTheSameShortCodeOnlyOneShouldSucceed() {
        Callable<String> shortCodeRequest = () -> {
            ShortenedUrlDto shortUrl = this.urlShortenerService.createShortUrl(createGoogleUrl(), "123abc", null);
            return shortUrl.code();
        };

        List<Future<String>> futures;
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            futures = executorService.invokeAll(List.of(shortCodeRequest, shortCodeRequest));
            executorService.shutdown();
        }

        assertThatThrownBy(() -> {
            for (Future<String> future : futures) {
                assertThat(future.get()).isEqualTo("123abc");
            }
        }).hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    @NotNull
    @SneakyThrows
    private static URL createGoogleUrl() {
        return new URI("https://google.com").toURL();
    }

}