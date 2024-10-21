package de.abat.shortener.infrastructure.generator;

public interface ShortUrlGenerator {

    String generate();

    String removeFromPool(String custom);
}
