package de.abat.shortener;

import de.abat.shortener.testcontainers.UrlShortenerServiceImplITestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UrlShortenerApplicationTests extends UrlShortenerServiceImplITestConfiguration {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        ApplicationModules applicationModules = ApplicationModules.of(UrlShortenerApplication.class);
        applicationModules.verify();
    }

    @Test
    void whenFinishedInitializationRedisShouldContain1089UniqueCodes() {
        Assertions.assertThat(redisTemplate.opsForSet().size("5-length")).isEqualTo(39135393L);
    }
}
