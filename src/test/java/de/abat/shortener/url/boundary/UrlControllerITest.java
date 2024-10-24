package de.abat.shortener.url.boundary;

import de.abat.shortener.testcontainers.UrlShortenerITestConfiguration;
import de.abat.shortener.url.entity.ShortenedUrl;
import de.abat.shortener.url.entity.ShortenedUrlRepository;
import de.abat.shortener.url.exceptions.ApplicationExceptionHandler;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlControllerITest extends UrlShortenerITestConfiguration {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ShortenedUrlRepository shortenedUrlRepository;

    @Test
    @SneakyThrows
    void shouldShortUrl() {
        String google = "https://google.com";
        ShortRequest request = new ShortRequest(new URI(google).toURL(), "ABCDE");

        ShortenedUrlDto shortResponse = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);
        assertThat(shortResponse.code()).hasSize(5);

        ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/" + shortResponse.code(), Object.class);
        assertThat(redirectResponse)
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.FOUND);
        assertThat(redirectResponse)
                .extracting(ResponseEntity::getHeaders)
                .extracting(HttpHeaders::getLocation)
                .isEqualTo(new URI(google));

        assertThat(shortenedUrlRepository.findByShortCodeIgnoreCase(shortResponse.code()))
                .get().extracting(ShortenedUrl::getUrl)
                .isEqualTo(google);
    }

    @Test
    @SneakyThrows
    void shouldShortCustomUrl() {
        String yahoo = "https://yahoo.com";
        String custom5LengthCode = "abcd1";
        ShortRequest request = new ShortRequest(new URI(yahoo).toURL(), custom5LengthCode);

        ShortenedUrlDto shortResponse = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);
        assertThat(shortResponse.code()).hasSize(5);

        ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/" + custom5LengthCode, Object.class);
        assertThat(redirectResponse)
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.FOUND);
        assertThat(redirectResponse)
                .extracting(ResponseEntity::getHeaders)
                .extracting(HttpHeaders::getLocation)
                .isEqualTo(new URI(yahoo));

        assertThat(shortenedUrlRepository.findByShortCodeIgnoreCase(shortResponse.code()))
                .get().extracting(ShortenedUrl::getUrl)
                .isEqualTo(yahoo);
    }

    @Test
    @SneakyThrows
    void shouldShortUrlWithTtl() {
        String bing = "https://bing.com";
        Duration ttl = Duration.ofSeconds(2);
        ShortRequest request = new ShortRequest(new URI(bing).toURL(), null, ttl);

        ShortenedUrlDto shortResponse = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);

        ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/" + shortResponse.code(), Object.class);
        assertThat(redirectResponse)
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.FOUND);
        assertThat(redirectResponse)
                .extracting(ResponseEntity::getHeaders)
                .extracting(HttpHeaders::getLocation)
                .isEqualTo(new URI(bing));

        ShortenedUrl shortenedUrl = shortenedUrlRepository.findByShortCodeIgnoreCase(shortResponse.code()).orElseThrow();
        Awaitility.await("for ttl to expire")
                .timeout(ttl.plusSeconds(1))
                .and()
                .pollDelay(ttl)
                .with()
                .untilAsserted(() -> {
                    assertThat(shortenedUrl.getUrl()).isEqualTo(bing);
                    assertThat(shortenedUrl.getValidUntil()).isBefore(ZonedDateTime.now());
                });
    }

    @Test
    @SneakyThrows
    void whenShortenedUrlExpiredShouldReturn404() {
        String duckduckgo = "https://duckduckgo.com";
        Duration ttl = Duration.ofSeconds(2);
        ShortRequest request = new ShortRequest(new URI(duckduckgo).toURL(), null, ttl);

        ShortenedUrlDto shortResponse = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);
        Awaitility.await("for ttl to expire")
                .timeout(ttl.plusSeconds(1))
                .and()
                .pollDelay(ttl)
                .with()
                .untilAsserted(() -> {
                    ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/" + shortResponse.code(), Object.class);
                    assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @SneakyThrows
    void whenCustomShortUrlDoesntExistShouldReturnClientError() {
        String searchGpt = "https://chatgpt.com/search";
        // GP code should always be available in Poll
        stringRedisTemplate.opsForSet().add("2-length", "GPT35");
        ShortRequest request = new ShortRequest(new URI(searchGpt).toURL(), "gpt35");

        var fireAndForget = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);

        //second time the code shouldn't be available
        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);
        assertThat(shortResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualToIgnoringCase("key gpt35 isn't available");
    }

    @Test
    @SneakyThrows
    void whenCustomShortUrlIsTakenShouldReturnClientError() {
        String searchGpt = "https://chatgpt.com/search";
        ShortRequest request = new ShortRequest(new URI(searchGpt).toURL(), "gpt41");

        var fireAndForget = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);

        //second time the code shouldn't be available
        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);
        assertThat(shortResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("key gpt41 isn't available");
    }

    @Test
    @SneakyThrows
    void whenCustomShortCodeIsNotInPoolShouldReturnClientError() {
        String searchGpt = "https://example.com";
        ShortRequest request = new ShortRequest(new URI(searchGpt).toURL(), "XCVAS");

        //remove code from Pool
        stringRedisTemplate.opsForSet().remove("5-length", "XCVAS");

        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);
        assertThat(shortResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("Key XCVAS doesn't exist in pool");
    }

    @Test
    @SneakyThrows
    void whenAnInternalErrorOccursShouldAdaptMessage() {
        ShortRequest request = new ShortRequest(null, "124AW", Duration.ofSeconds(1));

        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);

        assertThat(shortResponse.getStatusCode().is5xxServerError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("internal server error");
    }

}