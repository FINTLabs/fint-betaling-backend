package no.fint.betaling.controller;

import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InsufficientDataException;
import no.fint.betaling.exception.NoVISIDColumnException;
import no.fint.betaling.exception.UnableToReadFileException;
import no.fint.betaling.service.FileService;
import no.fint.betaling.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity getCustomersOnFile(@RequestHeader(name = "x-school-org-id") String schoolId, @RequestBody byte[] file) throws NoVISIDColumnException, UnableToReadFileException, InsufficientDataException, HttpMediaTypeNotAcceptableException {
        return ResponseEntity.ok(fileService.extractCustomerFileGroupFromSheet(
                fileService.getSheetFromBytes(file),
                groupService.getCustomersForSchoolWithVisIdKey(schoolId)));
    }

    @ExceptionHandler({InsufficientDataException.class})
    public ResponseEntity handleInsufficientDataException() {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "list is empty"));
    }

    @ExceptionHandler({NoVISIDColumnException.class})
    public ResponseEntity handleNoVisIdException() {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "missing VIS-ID column"));
    }

    @ExceptionHandler({UnableToReadFileException.class})
    public ResponseEntity handleUnableToReadFileException() {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "unable to read file"));
    }

    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
    public ResponseEntity handleUnsupportedMediaException(HttpMediaTypeNotAcceptableException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Collections.singletonMap("message", "invalid file type: " + ex.getMessage()));
    }
}