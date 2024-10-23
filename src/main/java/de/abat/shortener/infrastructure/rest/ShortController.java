package de.abat.shortener.infrastructure.rest;

import de.abat.shortener.url.boundary.ShortRequest;
import de.abat.shortener.url.boundary.ShortenedUrlDto;
import de.abat.shortener.url.boundary.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/short", ""})
public class ShortController {

    private final UrlShortenerService urlShortenerService;

    public ShortController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{5,}}")
    public ResponseEntity<?> get(@PathVariable String shortCode) {
        Optional<String> maybeUrl = urlShortenerService.getUrl(shortCode);
        return maybeUrl.map(s -> ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(s))
                .build()).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortenedUrlDto shortUrl(@Valid @RequestBody ShortRequest request) {
        return urlShortenerService.createShortUrl(request.url(), request.code(), request.ttl());
    }
}
