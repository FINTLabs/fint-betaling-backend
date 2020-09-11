package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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
    public CustomerGroup getCustomersOnFile(@RequestHeader(name = "x-school-org-id") String schoolId, @RequestBody MultipartFile file) throws IOException {
        //TODO: CHANGE NEW FILE PATHNAME TO NOT LOCAL
        File newFile = new File("/Users/perolastalberg/Desktop/tempfiler/" + file.getOriginalFilename());
        if (newFile.createNewFile()) {
            file.transferTo(newFile);
            return fileService.getCustomersFromFile(schoolId, newFile);
        }else{
            //TODO: HANDLE FILE ALLREADY EXISTS
            return null;
        }

    }
}
