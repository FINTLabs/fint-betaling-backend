package no.fint.betaling.common.exception;

import org.springframework.http.HttpStatusCode;

public class ClientErrorException extends RuntimeException{
    public ClientErrorException(HttpStatusCode statusCode) {
        super("Client error: Request returned a " + statusCode + " response.");
    }
}
