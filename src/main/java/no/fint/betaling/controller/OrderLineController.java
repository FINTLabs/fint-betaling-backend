package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.RestService;
import no.fint.model.administrasjon.okonomi.Varelinje;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/orderline")
public class OrderLineController {


    @Value("${fint.betaling.endpoints.orderLine}")
    private String orderLineEndpoint;

    @Autowired
    private RestService restService;

    @RequestMapping(method = GET)
    public ResponseEntity getAllOrderLines(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId) {
        return ResponseEntity.ok(restService.getResource(VarelinjeResources.class, orderLineEndpoint, orgId).getContent());
    }

    @RequestMapping(method = POST)
    public ResponseEntity setOrderLine(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                       @RequestBody VarelinjeResource orderLine) {
        return ResponseEntity.ok(restService.setResource(VarelinjeResource.class, orderLineEndpoint, orderLine, orgId));
    }
}
