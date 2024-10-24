package de.abat.shortener.events;

import java.util.UUID;

public record ShortUrlCreated(UUID shortenedUrl) {
}
