package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.OrderLineService;
import no.fint.model.administrasjon.okonomi.Varelinje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/orderline")
public class OrderLineController {

    @Autowired
    private OrderLineService orderLineService;

    @GetMapping
    public ResponseEntity getAllOrderlines(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId) {
        return ResponseEntity.ok(orderLineService.getOrderLines(orgId));
    }

    @PostMapping("/save")
    public ResponseEntity setOrderLine(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                       @RequestBody Varelinje orderLine) {
        return ResponseEntity.ok(orderLineService.setOrderLine(orgId, orderLine));
    }
}
