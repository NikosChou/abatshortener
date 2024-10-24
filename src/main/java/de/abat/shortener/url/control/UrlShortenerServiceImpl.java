package de.abat.shortener.url.control;

import de.abat.shortener.events.ShortUrlCreated;
import de.abat.shortener.events.ShortUrlVisited;
import de.abat.shortener.url.boundary.ShortenedUrlDto;
import de.abat.shortener.url.boundary.UrlShortenerService;
import de.abat.shortener.url.entity.ShortenedUrl;
import de.abat.shortener.url.entity.ShortenedUrlRepository;
import de.abat.shortener.url.exceptions.KeyNotExistsException;
import de.abat.shortener.url.pool.KeyPool;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
class UrlShortenerServiceImpl implements UrlShortenerService {

    final private KeyPool keyPool;
    final private ShortenedUrlRepository shortenedUrlRepository;
    final private ApplicationEventPublisher eventPublisher;

    public UrlShortenerServiceImpl(KeyPool keyPool, ShortenedUrlRepository shortenedUrlRepository, ApplicationEventPublisher eventPublisher) {
        this.keyPool = keyPool;
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<String> getUrl(String shortCode) {
        Optional<ShortenedUrl> maybeShortenedUrl = shortenedUrlRepository.findByShortCodeIgnoreCase(shortCode);
        log.debug("maybeShortenedUrl: {}", maybeShortenedUrl);
        maybeShortenedUrl.ifPresent(entity -> this.eventPublisher.publishEvent(new ShortUrlVisited(entity.getId())));
        return maybeShortenedUrl
                .filter(this::isShortUrlValid)
                .map(ShortenedUrl::getUrl);
    }

    @Override
    public ShortenedUrlDto createShortUrl(URL url, String customCode, Duration ttl) {
        if (Objects.nonNull(customCode)) {
            shortenedUrlRepository.findByShortCodeIgnoreCase(customCode).ifPresent(existing -> {
                throw new KeyNotExistsException(String.format("key %s isn't available", existing.getShortCode()));
            });
        }

        String shortCode = keyPool.pop(customCode);
        ZonedDateTime validUntil = Optional.ofNullable(ttl)
                .map(t -> ZonedDateTime.now().plus(t))
                .orElse(null);
        ShortenedUrl entity = shortenedUrlRepository.save(new ShortenedUrl(url.toString(), shortCode, validUntil));
        this.eventPublisher.publishEvent(new ShortUrlCreated(entity.getId()));
        return new ShortenedUrlDto(entity.getId(), url.toString(), shortCode, validUntil, entity.getCreatedAt());
    }

    boolean isShortUrlValid(ShortenedUrl s) {
        if (s == null) {
            return false;
        }

        ZonedDateTime validUntil = s.getValidUntil();
        return validUntil == null || ZonedDateTime.now().isBefore(validUntil);
    }
}
