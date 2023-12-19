package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InsufficientDataException;
import no.fint.betaling.exception.UnsupportedMediaTypeException;
import no.fint.betaling.exception.NoVISIDColumnException;
import no.fint.betaling.exception.UnableToReadFileException;
import no.fint.betaling.service.FileService;
import no.fint.betaling.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping(value = "/api/file")
public class FileController {

    private FileService fileService;

    private GroupService groupService;

    public FileController(FileService fileService, GroupService groupService) {
        this.fileService = fileService;
        this.groupService = groupService;
    }

    @PostMapping
    // TODO: Burde egentlig x-school-org-id vært en del av api url'en?
    public ResponseEntity getCustomersOnFile(@RequestHeader(name = "x-school-org-id") String schoolId, @RequestBody byte[] file) throws NoVISIDColumnException, UnableToReadFileException, InsufficientDataException, UnsupportedMediaTypeException {
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

    @ExceptionHandler({UnsupportedMediaTypeException.class})
    public ResponseEntity handleUnsupportedMediaException(UnsupportedMediaTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Collections.singletonMap("message", "invalid file type: " + ex.getMessage()));
    }
}