package no.fint.betaling.controller;

import no.fint.betaling.model.Principal;
import no.fint.betaling.repository.PrincipalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/principal")
public class PrincipalController {

    @Autowired
    private PrincipalRepository repository;

    @GetMapping
    public Collection<Principal> getPrincipals() {
        return repository.getPrincipals();
    }
}
