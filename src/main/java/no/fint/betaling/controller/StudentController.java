package no.fint.betaling.controller;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api")
public class StudentController {

    @Autowired
    private StudentService studentService;


    @GetMapping("/customers")
    public ResponseEntity getCustomers(@RequestParam(value = "etternavn", required = false) String lastName) {
        List<Kunde> allCustomers = studentService.getCustomers();
        if (lastName != null && !lastName.isEmpty()) { // TODO: flytt denne inn i StudentService
            return ResponseEntity.ok(allCustomers.stream().filter(customer ->
                    customer.getNavn().getEtternavn().toLowerCase().contains(lastName.toLowerCase())
            ).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(allCustomers);
    }
}
