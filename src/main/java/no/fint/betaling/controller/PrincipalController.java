package no.fint.betaling.controller;

import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/principal")
public class PrincipalController {

    @Autowired
    private RestUtil restUtil;

    @Value("${fint.betaling.endpoints.principal}")
    private URI principalEndpoint;

    @GetMapping
    public List<OppdragsgiverResource> getPrincipals(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return restUtil.get(OppdragsgiverResources.class, principalEndpoint, orgId).getContent();
    }
}
