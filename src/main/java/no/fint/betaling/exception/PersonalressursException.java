package no.fint.betaling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PersonalressursException extends ResponseStatusException {
    public PersonalressursException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
