package no.fint.betaling.controller;

import no.fint.betaling.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
public class MeController {

    @GetMapping
    public ResponseEntity getMe(){
        User user = new User();
        user.setName("Ola Nordmann");
        user.setOrganisation("Rogaland fylkeskommune");
        return ResponseEntity.ok(user);
    }
}
