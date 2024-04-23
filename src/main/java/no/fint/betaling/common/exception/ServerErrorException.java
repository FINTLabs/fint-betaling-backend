package no.fint.betaling.common.exception;

import org.springframework.http.HttpStatusCode;

public class ServerErrorException extends RuntimeException {
    public ServerErrorException(HttpStatusCode statusCode) {
        super("Server error: Request returned a " + statusCode + " response.");
    }
}
