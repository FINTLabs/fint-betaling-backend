package no.fint.betaling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SkoleressursException extends ResponseStatusException {
    public SkoleressursException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
