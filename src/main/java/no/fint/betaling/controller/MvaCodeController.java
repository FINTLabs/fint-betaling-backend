package no.fint.betaling.controller;

import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.administrasjon.okonomi.MvakodeResource;
import no.fint.model.resource.administrasjon.okonomi.MvakodeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/mva-code")
public class MvaCodeController {

    @Autowired
    private RestUtil restUtil;

    @Value("${fint.betaling.endpoints.mva-code}")
    private URI mvaEndpoint;

    @GetMapping
    public List<MvakodeResource> getMvaCodes(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return restUtil.get(MvakodeResources.class, mvaEndpoint, orgId).getContent();
    }
}
