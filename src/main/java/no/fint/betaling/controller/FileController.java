package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.service.FileService;
import no.fint.betaling.util.CustomerFileGroup;
import org.apache.tika.Tika;
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

        String contentType = new Tika().detect(file);

        if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || contentType.equals("application/vnd.ms-excel")) {
            CustomerFileGroup customersFromFile = fileService.getCustomersFromFile(schoolId, file);
            ResponseEntity<CustomerFileGroup> response = ResponseEntity.ok(customersFromFile);
            if (customersFromFile != null) {
                if (customersFromFile.getFoundCustomers() != null && customersFromFile.getNotFoundCustomers() != null) {
                    return response;
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{}");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("{}");
        }
    }
}
