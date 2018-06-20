package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Payment;
import no.fint.betaling.model.Varelinje;
import no.fint.betaling.service.OrderLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/orderline")
public class OrderLineController {

    @Autowired
    private OrderLineService orderLineService;

    @GetMapping
    public ResponseEntity getAllOrderlines(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId){
        return ResponseEntity.ok(orderLineService.getOrderLines(orgId));
    }

    @PostMapping("/save")
    public ResponseEntity setOrderLine(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                       @RequestBody Varelinje orderLine){
        return ResponseEntity.ok(orderLineService.setOrderLine(orgId, orderLine));
    }
}
