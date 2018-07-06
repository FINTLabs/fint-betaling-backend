package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.RestService;
import no.fint.model.resource.administrasjon.okonomi.MvakodeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/mvakode")
public class MvaCodeController {

    @Autowired
    private RestService restService;

    @Value("${fint.betaling.endpoints.mva-code}")
    private String mvaEndpoint;

    @GetMapping
    public ResponseEntity getMvaCodes(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId){
        return ResponseEntity.ok(restService.getResource(MvakodeResources.class, mvaEndpoint, orgId).getContent());
    }
}
