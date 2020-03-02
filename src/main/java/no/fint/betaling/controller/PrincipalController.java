package no.fint.betaling.controller;

import no.fint.betaling.model.Principal;
import no.fint.betaling.service.PrincipalService;
import org.springframework.web.bind.annotation.*;

import static no.fint.betaling.config.Config.DEFAULT_SCHOOL_ORG_ID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/principal")
public class PrincipalController {

    private final PrincipalService principalService;

    public PrincipalController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping
    public Principal getPrincipalForSchoolId(@RequestHeader(name = "x-school-org-id",
            defaultValue = DEFAULT_SCHOOL_ORG_ID) String schoolId) {
        return principalService.getPrincipalByOrganisationId(schoolId);
    }
}
