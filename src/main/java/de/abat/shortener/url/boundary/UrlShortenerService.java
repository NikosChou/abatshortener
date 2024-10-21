package de.abat.shortener.url.boundary;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

public interface UrlShortenerService {
    Optional<String> getUrl(String shortCode);

    ShortenedUrlDto createShortUrl(URL url, String code, Duration ttl);
}
