package de.abat.shortener.url.boundary;

import java.time.ZonedDateTime;

public record ShortenedUrlDto(String url, String code, ZonedDateTime validUntil, ZonedDateTime createdAt) {
}
