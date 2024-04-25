package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimsDatePeriod;
import no.fint.betaling.model.Order;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/claim")
public class ClaimController {

    private final ClaimDatabaseService claimDatabaseService;

    private final ClaimRestService claimRestService;

    private final ScheduleService scheduleService;

    public ClaimController(ClaimDatabaseService claimDatabaseService, ClaimRestService claimRestService, ScheduleService scheduleService) {
        this.claimDatabaseService = claimDatabaseService;
        this.claimRestService = claimRestService;
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<List<Claim>> storeClaim(@RequestBody Order order) {
        log.info("Received order: {}", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(claimDatabaseService.storeClaims(order));
    }

    @PostMapping("/send")
    public ResponseEntity<Flux<Claim>> sendClaims(@RequestBody List<Long> orderNumbers) {
        log.info("Send claims for order number: {}", orderNumbers);
        Flux<Claim> flux = claimRestService.sendClaims(orderNumbers);

        // TODO: 02/05/2023 CT-688 Denne nullsjekken bør være undøvendig. Trolig årsak til at det ikke fungerer:
        if (flux == null) {
            log.warn("In sendClaims, flux is null");
            flux = Flux.empty();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flux
                        .doOnNext(claim -> log.info("Sent claim: orderNumber: {} Status: {} CreatedDate: {}", claim.getOrderNumber(), claim.getClaimStatus(), claim.getCreatedDate()))
                        .switchIfEmpty(Flux.empty())
                        .onErrorMap(throwable -> {
                            log.error("Error occurred while sending claims", throwable);
                            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while sending claims", throwable);
                        })
                );
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims(@RequestParam(required = false) String periodSelection,
                                                    @RequestParam(required = false) String schoolSelection,
                                                    @RequestParam(required = false) String[] status) {

        return ResponseEntity.ok(
                claimDatabaseService.getClaimsByPeriodAndOrganisationnumberAndStatus(
                        toDatePeriod(periodSelection), schoolSelection, toClaimStatus(status))
        );
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<Claim>> getClaimsByCustomerName(@PathVariable String name) {
        return ResponseEntity.ok(claimDatabaseService.getClaimsByCustomerName(name));
    }

    @GetMapping("/order-number/{order-number}")
    public ResponseEntity<Claim> getClaimsByOrderNumber(@PathVariable(value = "order-number") long orderNumber) {
        return ResponseEntity.ok(claimDatabaseService.getClaimByOrderNumber(orderNumber));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Claim>> getClaimsByStatus(@PathVariable String[] status) {
        return ResponseEntity.ok(claimDatabaseService.getClaimsByStatus(toClaimStatus(status)));
    }

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Integer> getCountByStatus(@PathVariable String[] status,
                                                    @RequestParam(required = false) String days) {
        return ResponseEntity.ok(claimDatabaseService.countClaimsByStatus(toClaimStatus(status), days));
    }

    /**
     * @deprecated <p>This endpoint is being replaced by /count/status/{status}</p>
     */
    @Deprecated
    @GetMapping("/count/by-status/{status}")
    public ResponseEntity<Integer> getCountByStatusOld(@PathVariable String[] status,
                                                       @RequestParam(required = false) String days) {
        return ResponseEntity.ok(claimDatabaseService.countClaimsByStatus(toClaimStatus(status), days));
    }

    @DeleteMapping("/order-number/{order-number}")
    public ResponseEntity cancelClaimsByID(@PathVariable("order-number") long orderNumber) {
        claimDatabaseService.cancelClaim(orderNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/update/all")
    public void updateAll() {
        scheduleService.updateAcceptedClaims();
        scheduleService.updateIssuedClaims();
        scheduleService.updateUpdateErrorClaims();
    }

    @PostMapping(("update/status"))
    public Mono<ResponseEntity<Void>> updateClaimsStatus(@RequestParam(required = false) String periodSelection,
                                                          @RequestParam(required = false) String schoolSelection,
                                                          @RequestParam(required = false) String[] status) {

        return claimRestService
                .updateClaimStatus(toDatePeriod(periodSelection), schoolSelection, toClaimStatus(status))
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
    }

    private ClaimStatus[] toClaimStatus(String[] status) {
        if (status != null && status.length > 0) {
            return Arrays.stream(status).map(ClaimStatus::valueOf).toArray(ClaimStatus[]::new);
        } else {
            return null;
        }
    }

    private ClaimsDatePeriod toDatePeriod(String periodSelection) {
        if (StringUtils.isBlank(periodSelection)) periodSelection = ClaimsDatePeriod.ALL.name();
        return ClaimsDatePeriod.valueOf(periodSelection);
    }
}