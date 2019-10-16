package no.fint.betaling.controller;

import no.fint.betaling.model.Organization;
import no.fint.betaling.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
public class MeController {

    @GetMapping
    public User getMe(@RequestHeader(name = "x-ePPN", required = false) String ePPN) {

        //TODO  Get name, school owner (and schools?) from Feide/LDAP
        //      How to map from Feide orgId to Fint orgId
        //      systemid - 10261j

        User user = new User();

        user.setName("Navn Navnesen");

        Organization schoolOwner = new Organization();
        schoolOwner.setName("Telemark fylkeskommune");
        schoolOwner.setOrganizationNumber("");
        user.setOrganization(schoolOwner);

        Organization school1 = new Organization();
        school1.setName("Skien videregående skole");
        school1.setOrganizationNumber("974568039");

        Organization school2 = new Organization();
        school2.setName("Notodden videregående skole");
        school2.setOrganizationNumber("974568012");

        user.setSchools(Arrays.asList(school1, school2));

        return user;
    }
}
