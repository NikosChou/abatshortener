package de.abat.shortener.infrastructure.exceptions;

public class ShortExistsException extends RuntimeException {

    public ShortExistsException(String message) {
        super(message);
    }
}
