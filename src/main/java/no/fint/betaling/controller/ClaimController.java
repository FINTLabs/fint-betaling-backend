package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.Order;
import no.fint.betaling.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/claim")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping
    public ResponseEntity setClaim(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                   @RequestBody Order order) {
        log.info("{}: Received claim {}", orgId, order);
        // TODO missing location header?
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.setClaim(orgId, order));
    }

    @GetMapping
    public List<Claim> getAllClaims(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return claimService.getAllClaims(orgId);
    }

    @GetMapping("/name/{name}")
    public List<Claim> getClaimsByCustomerName(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                               @PathVariable(value = "name") String name) {
        return claimService.getClaimsByCustomerName(orgId, name);
    }

    @GetMapping("/order-number/{order-number}")
    public List<Claim> getClaimsByOrderNumber(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                              @PathVariable(value = "order-number") String orderNumber) {
        return claimService.getClaimsByOrderNumber(orgId, orderNumber);
    }

    @PostMapping("/send")
    public ResponseEntity sendClaims(@RequestHeader(name = ORG_ID, defaultValue = HeaderConstants.DEFAULT_VALUE_ORG_ID) String orgId,
                                     @RequestBody List<String> orderNumbers) {
        log.info("Send claims for {}", orgId);
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.sendClaims(orgId, orderNumbers));
    }

    @GetMapping("/update")
    public ResponseEntity updateClaims(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        log.info("Update Claim status for {}", orgId);
        claimService.updateClaimStatus(orgId);
        return ResponseEntity.noContent().build();
    }
}