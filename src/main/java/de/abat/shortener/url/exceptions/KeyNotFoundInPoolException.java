package de.abat.shortener.url.exceptions;

public class KeyNotFoundInPoolException extends RuntimeException {

    public KeyNotFoundInPoolException(String message) {
        super(message);
    }
}
