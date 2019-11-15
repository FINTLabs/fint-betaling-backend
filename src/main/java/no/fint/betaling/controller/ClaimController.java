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
    public ResponseEntity setClaim(@RequestBody Order order) {
        log.info("Received claim {}", order);
        // TODO missing location header?
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.setClaim(order));
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        return claimService.getAllClaims();
    }

    @GetMapping("/name/{name}")
    public List<Claim> getClaimsByCustomerName(@PathVariable(value = "name") String name) {
        return claimService.getClaimsByCustomerName(name);
    }

    @GetMapping("/order-number/{order-number}")
    public List<Claim> getClaimsByOrderNumber(@PathVariable(value = "order-number") String orderNumber) {
        return claimService.getClaimsByOrderNumber(orderNumber);
    }

    @PostMapping("/send")
    public ResponseEntity sendClaims(@RequestBody List<String> orderNumbers) {
        log.info("Send claims for ordernumbers: {}", orderNumbers);
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.sendClaims(orderNumbers));
    }

    /*
    @GetMapping("/update")
    public ResponseEntity updateClaims() {
        claimService.updateClaimStatus();
        return ResponseEntity.noContent().build();
    }
     */
}