package no.fint.betaling.exception;

public class InvalidResponseException extends RuntimeException {
    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
