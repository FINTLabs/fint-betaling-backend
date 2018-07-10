package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.RestService;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/oppdragsgiver")
public class EmployerController {

    @Autowired
    private RestService restService;

    @Value("${fint.betaling.endpoints.employer}")
    private String employerEndpoint;

    @GetMapping
    public ResponseEntity getEmployers(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-name}", required = false) String orgId){
        return ResponseEntity.ok(restService.getResource(OppdragsgiverResources.class, employerEndpoint, orgId).getContent());
    }
}