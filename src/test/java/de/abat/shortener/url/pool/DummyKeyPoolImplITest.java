package de.abat.shortener.url.pool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("defaultH2")
class DummyKeyPoolImplITest {

    @Autowired
    private DummyKeyPoolImpl defaultShortUrlGenerator;

    @Test
    void testBeanDefaultShortUrlGeneratorImplIsInitialized() {
        assertThat(defaultShortUrlGenerator).isNotNull();
    }
}