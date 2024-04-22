package no.fint.betaling.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoVISIDColumnException extends IOException {

}
