package no.fint.betaling.common.exception;

public class UnsupportedMediaTypeException extends Exception{
    public UnsupportedMediaTypeException(String contentType) {
        super(contentType);
    }
}
