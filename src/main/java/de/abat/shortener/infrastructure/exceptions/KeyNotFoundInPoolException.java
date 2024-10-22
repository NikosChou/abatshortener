package de.abat.shortener.infrastructure.exceptions;

public class KeyNotFoundInPoolException extends RuntimeException {

    public KeyNotFoundInPoolException(String message) {
        super(message);
    }
}
