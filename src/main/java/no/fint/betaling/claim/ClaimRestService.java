package no.fint.betaling.claim;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.util.FintClient;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.List;
import java.time.Duration;

@Slf4j
@Service
public class ClaimRestService {

    private final RestUtil restUtil;
    private final FintClient fintClient;
    private final InvoiceFactory invoiceFactory;
    private final ClaimRepository claimRepository;
    private final ClaimDatabaseService claimDatabaseService;
    private final ClaimRestStatusService claimRestStatusService;

    @Value("${fint.betaling.endpoints.invoice:/okonomi/faktura/fakturagrunnlag}")
    private String invoiceEndpoint;

    public ClaimRestService(RestUtil restUtil, FintClient fintClient, InvoiceFactory invoiceFactory, ClaimRepository claimRepository, ClaimDatabaseService claimDatabaseService, ClaimRestStatusService claimRestStatusService) {
        this.restUtil = restUtil;
        this.fintClient = fintClient;
        this.invoiceFactory = invoiceFactory;
        this.claimRepository = claimRepository;
        this.claimDatabaseService = claimDatabaseService;
        this.claimRestStatusService = claimRestStatusService;
    }

    public Flux<Claim> sendClaims(List<Long> orderNumbers) {
        return Flux.fromIterable(claimDatabaseService.getUnsentClaims())
                .filter(claim -> orderNumbers.contains(claim.getOrderNumber()))
                .flatMap(this::sendClaim)
                .onErrorResume(throwable -> {
                    log.error("Error occurred while sending claims", throwable);
                    return Flux.error(throwable);
                });
    }

    private Mono<Claim> sendClaim(Claim claim) {
        FakturagrunnlagResource invoice = invoiceFactory.createInvoice(claim);

        return restUtil.post(invoiceEndpoint, invoice, FakturagrunnlagResource.class)
                .doOnNext(httpHeaders -> {
                    if (httpHeaders.getLocation() != null) {
                        claim.setInvoiceUri(httpHeaders.getLocation().toString());
                    } else {
                        log.error("Successfull POST to FINT, but no location header in response");
                    }

                    claim.setClaimStatus(ClaimStatus.SENT);
                    claim.setStatusMessage(null);
                    log.info("Claim {} sent, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                    claimRepository.save(claim);
                })
                .doOnError(WebClientResponseException.class, ex -> {
                    claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                    claim.setStatusMessage(ex.getMessage());
                    log.error("Error sending claim {}: {}", claim.getOrderNumber(), ex.getStatusCode());
                    claimRepository.save(claim);
                })
                .doOnSuccess(httpHeaders -> claimRestStatusService.processRequest(claim))
                .thenReturn(claim);
    }

    public void updateSentClaims() {
        claimDatabaseService.getSentClaims().forEach(claim -> {
            restUtil.head(claim.getInvoiceUri())
                    .doOnNext(headers -> {
                        if (headers.getLocation() != null) {
                            claim.setInvoiceUri(headers.getLocation().toString());
                            claim.setClaimStatus(ClaimStatus.ACCEPTED);
                            log.info("Claim {} accepted, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                        } else {
                            log.info("Claim {} not updated", claim.getOrderNumber());
                        }
                        claim.setStatusMessage(null);
                        claimRepository.save(claim);
                    })
                    .doOnError(WebClientResponseException.class, e -> {
                        if (e.getStatusCode() == HttpStatus.GONE) {
                            log.info("Claim {} gone from consumer -- retry sending!", claim.getOrderNumber());
                            claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                            claim.setInvoiceUri(null);
                            claimRepository.save(claim);
                        } else {
                            setClaimStatusFromFint(claim);
                        }
                    })
                    .doOnError(e -> {
                        log.warn("Error updating claim {} [{}]", claim.getOrderNumber(), claim.getClaimStatus());
                        log.error("Exception: " + e.getMessage(), e);
                    })
                    .subscribe();
        });
    }

    private void setClaimStatusFromFint(Claim claim) {
        restUtil.get(String.class, claim.getInvoiceUri())
                .flatMap(result -> {
                    log.error("Unexpected result! {}", result);
                    return Mono.empty(); // Brukes når det ikke er behov for å returnere en verdi fra flatMap
                })
                .doOnError(WebClientResponseException.class, e2 -> {
                    if (e2.getStatusCode().is4xxClientError()) {
                        claim.setClaimStatus(ClaimStatus.ACCEPT_ERROR);
                    } else if (e2.getStatusCode().is5xxServerError()) {
                        claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                    }
                    claim.setStatusMessage(e2.getMessage());
                    claimRepository.save(claim);
                    log.warn("Error accepting claim {} [{}]: [{}] {}", claim.getOrderNumber(), claim.getClaimStatus(), e2.getStatusCode(), e2.getMessage());
                })
                .onErrorResume(e -> Mono.empty()) // Håndterer andre feil og fortsetter
                .subscribe();
    }

    public void updateAcceptedClaims() {
        // TODO Accepted claims should be checked less often
        claimDatabaseService.getClaimsToRecheckStatus().forEach(claim -> {

            restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri())
                    .doOnNext(this::updateClaim)
                    .doOnError(WebClientResponseException.class, e -> {
                        claim.setClaimStatus(ClaimStatus.UPDATE_ERROR);
                        claim.setStatusMessage(e.getMessage());
                        log.error("Error updating claim {}: [{}] {}", claim.getOrderNumber(), e.getStatusCode(), e.getMessage());
                    })
                    .doOnError(e -> {
                        log.warn("Error updating claim {} [{}]", claim.getOrderNumber(), claim.getClaimStatus());
                        log.error("Exception: " + e.getMessage(), e);
                    })
                    .subscribe();
        });
    }

    /**
     * TODO Needs to update with both {@link FakturagrunnlagResource} and {@link FakturaResource}
     * "invoiceNumbers": ?
     * "invoiceDate": ?
     * "paymentDueDate": ?
     * "creditNotes": ?
     * - Check if claim is paid and set status accordingly to remove from update loop
     *
     * @return New claim status
     */
    private void updateClaim(FakturagrunnlagResource invoice) {
        Claim claim = claimRepository.get(Long.parseLong(invoice.getOrdrenummer().getIdentifikatorverdi()));
        fintClient.setInvoiceUri(invoice).ifPresent(claim::setInvoiceUri);

        Mono<List<FakturaResource>> fakturaListMono = fintClient.getFaktura(invoice);
        fakturaListMono.subscribe(fakturaList -> {
            updateClaimStatus(fakturaList, claim);
            updateClaimDate(claim, fakturaList);
            claimRepository.save(claim);
        });
        log.info("Claim {} updated", claim.getOrderNumber());
    }

    private void updateClaimDate(Claim claim, List<FakturaResource> fakturaList) {
        claim.setInvoiceNumbers(fintClient.getFakturanummere(fakturaList));
        fintClient.getInvoiceDate(fakturaList).ifPresent(claim::setInvoiceDate);
        fintClient.getPaymentDueDate(fakturaList).ifPresent(claim::setPaymentDueDate);
        fintClient.getAmountDue(fakturaList).ifPresent(claim::setAmountDue);
    }

    private void updateClaimStatus(List<FakturaResource> fakturaList, Claim claim) {
        boolean credited = fakturaList.stream().allMatch(FakturaResource::getKreditert);
        boolean paid = fakturaList.stream().allMatch(FakturaResource::getBetalt);
        boolean issued = fakturaList.stream().allMatch(FakturaResource::getFakturert);
        if (fakturaList.isEmpty()) {
            claim.setClaimStatus(ClaimStatus.ACCEPTED);
        } else if (credited || paid) {
            claim.setClaimStatus(ClaimStatus.PAID);
        } else if (issued) {
            claim.setClaimStatus(ClaimStatus.ISSUED);
        } else {
            claim.setClaimStatus(ClaimStatus.ACCEPTED);
        }
    }

}
