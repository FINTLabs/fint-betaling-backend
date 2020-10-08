package no.fint.betaling.controller;

import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.NoVISIDColumnException;
import no.fint.betaling.exception.UnableToReadFileException;
import no.fint.betaling.service.FileService;
import no.fint.betaling.util.CustomerFileGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping
    public ResponseEntity getCustomersOnFile(@RequestHeader(name = "x-school-org-id") String schoolId, @RequestBody byte[] file) throws NoVISIDColumnException, UnableToReadFileException {
        CustomerFileGroup customersFromFile = fileService.getCustomersFromFile(schoolId, file);
        if (customersFromFile.hasCustomers())
            return ResponseEntity.ok(customersFromFile);
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "could not find VIS-ID or list is empty"));
    }

    @ExceptionHandler({NoVISIDColumnException.class})
    public ResponseEntity handleNoVisIdException() {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "missing VIS-ID column"));
    }

    @ExceptionHandler({UnableToReadFileException.class})
    public ResponseEntity handleUnableToReadFileException() {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "unable to read file"));
    }

    @ExceptionHandler({UnsupportedMediaException.class})
    public ResponseEntity handleUnsupportedMediaException(UnsupportedMediaException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Collections.singletonMap("message", ex.getMessage()));
    }
}