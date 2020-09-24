package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.service.FileService;
import no.fint.betaling.util.CustomerFileGroup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity getCustomersOnFile(@RequestHeader(name = "x-school-org-id") String schoolId, @RequestBody byte[] file) {
        //Todo: Fix unsuported media type and wrong content
        ResponseEntity<CustomerFileGroup> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            response = ResponseEntity.ok(fileService.getCustomersFromFile(schoolId, file));
        }catch (Exception ioe){
            ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Supported files are of type: .xls and .xlsx");
        }
        return response;
    }
}
