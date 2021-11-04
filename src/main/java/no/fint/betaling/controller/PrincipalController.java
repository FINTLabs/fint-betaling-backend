package no.fint.betaling.controller;

import no.fint.betaling.model.Principal;
import no.fint.betaling.service.PrincipalService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/principal")
public class PrincipalController {

    private final PrincipalService principalService;

    public PrincipalController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping
    public Principal getPrincipalForSchoolId(@RequestHeader(name = "x-school-org-id") String schoolId,
                                             @RequestHeader(name = "x-feide-upn") String feideUpn) {
        return principalService.getPrincipalByOrganisationId(schoolId, feideUpn);
    }
}
