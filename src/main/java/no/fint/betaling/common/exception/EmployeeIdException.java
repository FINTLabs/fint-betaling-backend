package no.fint.betaling.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmployeeIdException extends ResponseStatusException {

    public EmployeeIdException(HttpStatus status, String message) {
        super(status, message);
    }
}
