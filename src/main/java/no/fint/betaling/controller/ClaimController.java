package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Order;
import no.fint.betaling.service.ClaimService;
import no.fint.betaling.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/claim")
public class ClaimController {

    private final ClaimService claimService;
    private final ScheduleService scheduleService;

    public ClaimController(ClaimService claimService, ScheduleService scheduleService) {
        this.claimService = claimService;
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<?> storeClaim(@RequestBody Order order) {
        log.info("Received order: {}", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.storeClaims(order));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendClaims(@RequestBody List<String> orderNumbers) {
        log.info("Send claims for order number: {}", orderNumbers);
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.sendClaims(orderNumbers));
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        return claimService.getClaims();
    }

    @GetMapping("/name/{name}")
    public List<Claim> getClaimsByCustomerName(@PathVariable(value = "name") String name) {
        return claimService.getClaimsByCustomerName(name);
    }

    @GetMapping("/order-number/{order-number}")
    public List<Claim> getClaimsByOrderNumber(@PathVariable(value = "order-number") String orderNumber) {
        return claimService.getClaimsByOrderNumber(orderNumber);
    }

    @GetMapping("/status/{status}")
    public List<Claim> getClaimsByStatus(@PathVariable("status") String[] status) {
        return claimService.getClaimsByStatus(Arrays.stream(status).map(ClaimStatus::valueOf).toArray(ClaimStatus[]::new));
    }

    @DeleteMapping("/order-number/{order-number}")
    public ResponseEntity cancelClaimsByID(@PathVariable("order-number") String orderNumber) {
        claimService.cancelClaim(orderNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/update/all")
    public void updateAll() {
        scheduleService.updateClaims();
    }
}