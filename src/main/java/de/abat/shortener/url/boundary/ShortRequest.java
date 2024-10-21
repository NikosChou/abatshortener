package de.abat.shortener.url.boundary;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URL;
import java.time.Duration;

public record ShortRequest(
        @Schema(type = "string", format = "url", example = "https://google.com") URL url,
        @Schema(type = "string", format = "string", example = "abcde", nullable = true) String code,
        @Schema(type = "string", format = "duration", example = "PT20S", nullable = true) Duration ttl) {

    public ShortRequest(URL url) {
        this(url, null, null);
    }

    public ShortRequest(URL url, String code) {
        this(url, code, null);
    }

    public ShortRequest(URL url, String code, Duration ttl) {
        this.url = url;
        this.code = code;
        this.ttl = ttl;
    }
}
