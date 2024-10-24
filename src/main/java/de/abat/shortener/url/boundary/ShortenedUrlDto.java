package de.abat.shortener.url.boundary;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ShortenedUrlDto(UUID id, String url, String code, ZonedDateTime validUntil, ZonedDateTime createdAt) {
}
