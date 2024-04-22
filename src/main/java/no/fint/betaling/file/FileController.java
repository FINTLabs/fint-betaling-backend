package no.fint.betaling.file;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.InsufficientDataException;
import no.fint.betaling.common.exception.UnsupportedMediaTypeException;
import no.fint.betaling.common.exception.NoVISIDColumnException;
import no.fint.betaling.common.exception.UnableToReadFileException;
import no.fint.betaling.group.GroupService;
import no.fint.betaling.common.util.CustomerFileGroup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping(value = "/file")
public class FileController {

    private final FileService fileService;

    private final GroupService groupService;

    public FileController(FileService fileService, GroupService groupService) {
        this.fileService = fileService;
        this.groupService = groupService;
    }

    @PostMapping
    // TODO: Burde egentlig x-school-org-id vært en del av api url'en?
    public ResponseEntity<CustomerFileGroup> getCustomersOnFile(@RequestHeader(name = "x-school-org-id") String schoolId, @RequestBody byte[] file) throws NoVISIDColumnException, UnableToReadFileException, InsufficientDataException, UnsupportedMediaTypeException {
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