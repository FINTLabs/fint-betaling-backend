package no.fint.betaling.controller;

import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
public class MeController {

    @GetMapping
    public User getMe(@RequestHeader(name = "x-ePPN", required = false) String ePPN) {

        //TODO Get name, school owner and schools from Feide/LDAP
        //TODO Implement lookup service for Fint Person

        User user = new User();

        user.setName("Navn Navnesen");

        Organisation schoolOwner = new Organisation();
        schoolOwner.setName("Test fylkeskommune");
        schoolOwner.setOrganisationNumber("888888888");
        user.setOrganisation(schoolOwner);

        Organisation school = new Organisation();
        school.setName("Jalla videregående skole");
        school.setOrganisationNumber("999999999");

        user.setOrganisationUnits(Collections.singletonList(school));

        return user;
    }
}
