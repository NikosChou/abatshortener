package de.abat.shortener.url.entity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ShortenedUrl> findByShortCodeIgnoreCase(String shortCode);
}
