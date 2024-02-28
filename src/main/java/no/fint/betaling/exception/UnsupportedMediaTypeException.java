package no.fint.betaling.exception;

public class UnsupportedMediaTypeException extends Exception{
    public UnsupportedMediaTypeException(String contentType) {
        super(contentType);
    }
}
