package de.abat.shortener.statistics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @NotNull
    private UUID shortenedUrl;
    private int count;
    private ZonedDateTime createdAt;

    public Statistics(UUID id, UUID shortenedUrl) {
        this.id = id;
        this.shortenedUrl = shortenedUrl;
    }

    @PrePersist
    void preInsert() {
        this.createdAt = ZonedDateTime.now();
        this.count = 0;
    }


}
