package no.fint.betaling.controller;

import no.fint.betaling.service.RestService;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/oppdragsgiver")
public class EmployerController {

    @Autowired
    private RestService restService;

    @Value("${fint.betaling.endpoints.employer}")
    private String employerEndpoint;

    @GetMapping
    public List<OppdragsgiverResource> getEmployers(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return restService.getResource(OppdragsgiverResources.class, employerEndpoint, orgId).getContent();
    }
}
