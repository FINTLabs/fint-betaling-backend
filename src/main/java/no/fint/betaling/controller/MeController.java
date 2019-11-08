package no.fint.betaling.controller;

import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

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
        schoolOwner.setName("Telemark fylkeskommune");
        schoolOwner.setOrganisationNumber("");
        user.setOrganisation(schoolOwner);

        Organisation school1 = new Organisation();
        school1.setName("Skien videregående skole");
        school1.setOrganisationNumber("974568039");

        Organisation school2 = new Organisation();
        school2.setName("Notodden videregående skole");
        school2.setOrganisationNumber("974568012");

        user.setOrganisationUnits(Arrays.asList(school1, school2));

        return user;
    }
}
