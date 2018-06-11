package no.fint.betaling.model;

public class InvalidResponseException extends RuntimeException {
    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
