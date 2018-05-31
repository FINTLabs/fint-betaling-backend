package no.fint.betaling.controller;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.service.GroupService;
import no.fint.betaling.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/customers")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private GroupService groupService;

    @GetMapping("/customers")
    public ResponseEntity getCustomers(@RequestParam(value = "etternavn", required = false) String lastName) {
        List<Kunde> allCustomers = studentService.getCustomers();
        if (lastName != null && !lastName.isEmpty()) {
            return ResponseEntity.ok(allCustomers.stream().filter(customer ->
                    customer.getNavn().getEtternavn().toLowerCase().contains(lastName.toLowerCase())
            ).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(allCustomers);
    }

    @GetMapping("/group")
    public ResponseEntity getCustomerGroups(){
        List<KundeGruppe> customerGroups = groupService.getCustomerGroups();
        return ResponseEntity.ok(customerGroups);
    }
}
