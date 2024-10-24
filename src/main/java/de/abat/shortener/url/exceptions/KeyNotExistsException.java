package de.abat.shortener.url.exceptions;

public class KeyNotExistsException extends RuntimeException {

    public KeyNotExistsException(String message) {
        super(message);
    }
}
