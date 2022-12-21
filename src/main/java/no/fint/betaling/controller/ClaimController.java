package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimsDatePeriod;
import no.fint.betaling.model.Order;
import no.fint.betaling.service.ClaimService;
import no.fint.betaling.service.ScheduleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/claim")
public class ClaimController {

    private ClaimService claimService;

    private ScheduleService scheduleService;

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
    public List<Claim> getAllClaims(@RequestParam(required = false) String periodSelection,
                                    @RequestParam(required = false) String schoolSelection,
                                    @RequestParam(required = false) String[] status) throws ParseException {

        if (StringUtils.isBlank(periodSelection) && StringUtils.isBlank(schoolSelection)){
            return claimService.getClaims();
        } else {
            if (StringUtils.isBlank(periodSelection)) periodSelection = ClaimsDatePeriod.ALL.name();
            ClaimsDatePeriod period = ClaimsDatePeriod.valueOf(periodSelection);
            return claimService.getClaims(period, schoolSelection, toClaimStatus(status));
        }
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
        return claimService.getClaimsByStatus(toClaimStatus(status));
    }

    @GetMapping("/count/by-status/{status}")
    public int getCountByStatus(@PathVariable("status") String[] status) {
        return claimService.countClaimsByStatus(toClaimStatus(status));
    }

    @GetMapping("/count/by-status/{status}/{maximumDaysOld}")
    public int getCountByStatusAndMaximumDaysOld(@PathVariable("status") String[] status,
                                                 @PathVariable("maximumDaysOld") String maximumDaysOld) {
        return claimService.countClaimsByStatusAndMaximumDaysOld(toClaimStatus(status), maximumDaysOld);
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

    private ClaimStatus[] toClaimStatus(String[] status) {
        if(status != null && status.length > 0)
            return Arrays.stream(status).map(ClaimStatus::valueOf).toArray(ClaimStatus[]::new);
        else
            return null;
    }
}