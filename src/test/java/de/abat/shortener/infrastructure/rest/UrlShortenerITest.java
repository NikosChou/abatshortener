package de.abat.shortener.infrastructure.rest;

import de.abat.shortener.TestcontainersConfiguration;
import de.abat.shortener.url.boundary.ShortRequest;
import de.abat.shortener.url.boundary.ShortenedUrlDto;
import de.abat.shortener.url.entity.ShortenedUrl;
import de.abat.shortener.url.entity.ShortenedUrlRepository;
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
class UrlShortenerITest extends TestcontainersConfiguration {

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
        ShortRequest request = new ShortRequest(new URI(google).toURL());

        ShortenedUrlDto shortResponse = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);
        assertThat(shortResponse.code()).hasSize(2);

        ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/"+ shortResponse.code(), Object.class);
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
        String custom5LengthCode = "abcde";
        ShortRequest request = new ShortRequest(new URI(yahoo).toURL(), custom5LengthCode);

        ShortenedUrlDto shortResponse = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);
        assertThat(shortResponse.code()).hasSize(5);

        ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/"+ custom5LengthCode, Object.class);
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

        ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/"+ shortResponse.code(), Object.class);
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
                    ResponseEntity<Object> redirectResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/short/"+ shortResponse.code(), Object.class);
                    assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @SneakyThrows
    void whenCustomShortUrlDoesntExistShouldReturnClientError() {
        String searchGpt = "https://chatgpt.com/search";
        // GP code should always be available in Poll
        stringRedisTemplate.opsForSet().add("2-length", "GP");
        ShortRequest request = new ShortRequest(new URI(searchGpt).toURL(), "gp");

        var fireAndForget = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);

        //second time the code shouldn't be available
        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);
        assertThat(shortResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("Short customCode gp isn't available");
    }

    @Test
    @SneakyThrows
    void whenCustomShortUrlIsTakenShouldReturnClientError() {
        String searchGpt = "https://chatgpt.com/search";
        ShortRequest request = new ShortRequest(new URI(searchGpt).toURL(), "gpt");

        var fireAndForget = restTemplate.postForObject("http://localhost:" + port + "/api/v1/short", request, ShortenedUrlDto.class);

        //second time the code shouldn't be available
        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);
        assertThat(shortResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("Short customCode gpt isn't available");
    }

    @Test
    @SneakyThrows
    void whenCustomShortCodeIsNotInPoolShouldReturnClientError() {
        String searchGpt = "https://example.com";
        ShortRequest request = new ShortRequest(new URI(searchGpt).toURL(), "FS");

        //remove code from Pool
        stringRedisTemplate.opsForSet().remove("2-length", "FS");

        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/short", request, ApplicationExceptionHandler.ExceptionMessage.class);
        assertThat(shortResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("Short code FS doesn't exist in pool");
    }

    @Test
    @SneakyThrows
    void whenAnInternalErrorOccursShouldAdaptMessage() {
        ResponseEntity<ApplicationExceptionHandler.ExceptionMessage> shortResponse = restTemplate.getForEntity("http://localhost:" + port + "/", ApplicationExceptionHandler.ExceptionMessage.class);

        assertThat(shortResponse.getStatusCode().is5xxServerError()).isTrue();
        assertThat(shortResponse.getBody().message()).isEqualTo("internal server error");
    }

}