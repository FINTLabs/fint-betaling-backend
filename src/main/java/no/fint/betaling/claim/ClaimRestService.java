package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.util.FintClient;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    public void updateClaims(ClaimStatus claimStatus, Duration maxAge) {
        claimDatabaseService.getClaimsByStatus(claimStatus).forEach(claim -> {
            if (maxAge == null || isNewerThan(claim, maxAge)) {
                updateClaim(claim);
            } else {
                log.debug("Claim {} not updated, older than {}", claim.getOrderNumber(), maxAge);
            }
        });
    }

    private void updateClaim(Claim claim) {
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

    private boolean isNewerThan(Claim claim, Duration maxAge) {
        return maxAge == null || Duration.between(claim.getCreatedDate(), LocalDateTime.now()).compareTo(maxAge) > 0;
    }
}
