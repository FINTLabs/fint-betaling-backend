package no.fint.betaling.exception;

import org.springframework.http.HttpStatus;

public class InvalidResponseException extends RuntimeException {
    private final HttpStatus status;
    public InvalidResponseException(HttpStatus statusCode, String message, Throwable cause) {
        super(message, cause);
        status = statusCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
