package no.fint.betaling.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SchoolNotFoundException extends RuntimeException {

    public SchoolNotFoundException(String message) {
        super(message);
    }
}
