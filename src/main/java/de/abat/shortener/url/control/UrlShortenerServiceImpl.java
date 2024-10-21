package de.abat.shortener.url.control;

import de.abat.shortener.infrastructure.exceptions.ShortExistsException;
import de.abat.shortener.infrastructure.generator.ShortUrlGenerator;
import de.abat.shortener.url.boundary.ShortenedUrlDto;
import de.abat.shortener.url.boundary.UrlShortenerService;
import de.abat.shortener.url.entity.ShortenedUrl;
import de.abat.shortener.url.entity.ShortenedUrlRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class UrlShortenerServiceImpl implements UrlShortenerService {

    final private ShortUrlGenerator shortGenerator;
    final private ShortenedUrlRepository shortenedUrlRepository;

    public UrlShortenerServiceImpl(ShortUrlGenerator shortGenerator, ShortenedUrlRepository shortenedUrlRepository) {
        this.shortGenerator = shortGenerator;
        this.shortenedUrlRepository = shortenedUrlRepository;
    }

    @Override
    public Optional<String> getUrl(String shortCode) {
        Optional<ShortenedUrl> maybeShortenedUrl = shortenedUrlRepository.findByShortCodeIgnoreCase(shortCode);
        log.debug("maybeShortenedUrl: {}", maybeShortenedUrl);
        return maybeShortenedUrl
                .filter(this::isShortUrlValid)
                .map(ShortenedUrl::getUrl);
    }

    @Override
    public ShortenedUrlDto createShortUrl(URL url, String customCode, Duration ttl) {
        if (Objects.nonNull(customCode)) {
            shortenedUrlRepository.findByShortCodeIgnoreCase(customCode).ifPresent(existing -> {
                throw new ShortExistsException(String.format("Short customCode %s isn't available", existing.getShortCode()));
            });
        }

        String shortCode = Optional.ofNullable(customCode)
                .map(shortGenerator::removeFromPool)
                .orElseGet(shortGenerator::generate);
        ZonedDateTime validUntil = Optional.ofNullable(ttl)
                .map(t -> ZonedDateTime.now().plus(t))
                .orElse(null);
        ShortenedUrl entity = shortenedUrlRepository.save(new ShortenedUrl(url.toString(), shortCode, validUntil));
        return new ShortenedUrlDto(url.toString(), shortCode, validUntil, entity.getCreatedAt());
    }

    boolean isShortUrlValid(ShortenedUrl s) {
        if (s == null) {
            return false;
        }

        ZonedDateTime validUntil = s.getValidUntil();
        return validUntil == null || ZonedDateTime.now().isBefore(validUntil);
    }
}
