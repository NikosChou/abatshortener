package de.abat.shortener.url.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShortenedUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String url;
    @Column(unique = true)
    private String shortCode;
    private ZonedDateTime validUntil;
    private ZonedDateTime createdAt;

    @PrePersist
    void preInsert() {
        this.createdAt = ZonedDateTime.now();
    }

    public ShortenedUrl(String url, String shortCode, ZonedDateTime validUntil) {
        this.url = url;
        this.shortCode = shortCode;
        this.validUntil = validUntil;
    }
}
