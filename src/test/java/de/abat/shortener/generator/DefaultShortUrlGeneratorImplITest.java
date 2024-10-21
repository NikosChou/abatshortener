package de.abat.shortener.generator;

import de.abat.shortener.infrastructure.generator.DefaultShortUrlGeneratorImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("defaultH2")
class DefaultShortUrlGeneratorImplITest {

    @Autowired
    private DefaultShortUrlGeneratorImpl defaultShortUrlGenerator;

    @Test
    void testBeanDefaultShortUrlGeneratorImplIsInitialized() {
        assertThat(defaultShortUrlGenerator).isNotNull();
    }
}