package de.abat.shortener.infrastructure.exceptions;

public class KeyNotExistsException extends RuntimeException {

    public KeyNotExistsException(String message) {
        super(message);
    }
}
