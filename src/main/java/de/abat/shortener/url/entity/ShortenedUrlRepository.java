package de.abat.shortener.url.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, UUID> {

    Optional<ShortenedUrl> findByShortCodeIgnoreCase(String shortCode);
}
