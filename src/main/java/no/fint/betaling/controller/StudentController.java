package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api")
public class StudentController {

    @Autowired
    private StudentService studentService;


    @GetMapping("/customer")
    public ResponseEntity getCustomers(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId, @RequestParam(value = "etternavn", required = false) String lastName) {
        List<Kunde> allCustomers = studentService.getCustomers(orgId, lastName);
        return ResponseEntity.ok(allCustomers);
    }
}
