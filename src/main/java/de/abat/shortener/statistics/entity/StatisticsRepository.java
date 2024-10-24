package de.abat.shortener.statistics.entity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<Statistics, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Statistics findByShortenedUrl(UUID id);
}
