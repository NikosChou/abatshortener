package de.abat.shortener.infrastructure.rest;

import de.abat.shortener.infrastructure.exceptions.KeyNotExistsException;
import de.abat.shortener.infrastructure.exceptions.KeyNotFoundInPoolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseBody
@ControllerAdvice
public class ApplicationExceptionHandler {
    public record ExceptionMessage(String message) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({KeyNotExistsException.class, KeyNotFoundInPoolException.class})
    public ExceptionMessage handleKeyException(Exception e) {
        log.error("Exception Handler", e);
        String message = "internal server error";
        if (e instanceof KeyNotFoundInPoolException sge) {
            message = sge.getMessage();
        } else if (e instanceof KeyNotExistsException see) {
            message = see.getMessage();
        }
        return new ExceptionMessage(message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ExceptionMessage handleException(Exception e) {
        log.error("Exception Handler", e);
        return new ExceptionMessage("internal server error");
    }
}
