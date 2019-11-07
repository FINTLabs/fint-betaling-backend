package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/lineitem")
public class LineitemController {

    @Value("${fint.betaling.endpoints.lineitem}")
    private URI LineitemEndpoint;

    @Autowired
    private RestUtil restUtil;

    @RequestMapping(method = GET)
    public List<VarelinjeResource> getAllLineitems(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return restUtil.get(VarelinjeResources.class, LineitemEndpoint, orgId).getContent();
    }
}
