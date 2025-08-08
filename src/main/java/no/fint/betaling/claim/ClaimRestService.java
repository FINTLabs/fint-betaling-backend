package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.util.FintClient;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimsDatePeriod;
import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    public Mono<Claim> sendClaim(Claim claim) {
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

    public void updateClaimsByStatusAndAge(ClaimStatus status, Duration maxAge) {
        List<Claim> claims = claimDatabaseService.getClaimsByStatus(status)
                .stream()
                .filter(claim -> maxAge == null || isNewerThan(claim, maxAge))
                .toList();

        log.info("Updating status for {} claims with status {} and max age {} days", claims.size(), status, maxAge.toDays());
        Flux.fromIterable(claims)
                .flatMap(this::updateClaimStatus)
                .doOnError(error -> log.error("Failed to update claim status", error))
                .doOnComplete(() -> log.info("Completed updating claims - triggered by schedule"))
                .subscribe();
    }

    public void updateClaimsOlderThanOneHour(ClaimStatus currentStatus) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<Claim> claims = claimDatabaseService.getClaimsByStatus(currentStatus)
                .stream()
                .filter(c -> c.getCreatedDate() != null && c.getCreatedDate().isBefore(threshold))
                .toList();

        log.info("Updating status for {} claims created before {} (older than 1h), from {} to {}",
                claims.size(), threshold, currentStatus, ClaimStatus.SEND_ERROR);

        Flux.fromIterable(claims)
                .flatMap(claim -> {
                    claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                    return updateClaimStatus(claim);
                })
                .doOnError(error -> log.error("Failed to update claim status", error))
                .doOnComplete(() -> log.info("Completed updating claims - triggered by schedule"))
                .subscribe();
    }

    public Flux<Claim> updateClaimsForPeriodAndOrganisation(ClaimsDatePeriod period, String organisationNumber, ClaimStatus[] statuses) {
        List<Claim> claims = claimDatabaseService
                .getClaimsByPeriodAndOrganisationnumberAndStatus(period, organisationNumber, statuses)
                .stream()
                .filter(claim -> claim.getClaimStatus() == ClaimStatus.ACCEPTED ||
                        claim.getClaimStatus() == ClaimStatus.ISSUED ||
                        claim.getClaimStatus() == ClaimStatus.PAID ||
                        claim.getClaimStatus() == ClaimStatus.CREDITED ||
                        claim.getClaimStatus() == ClaimStatus.UPDATE_ERROR)
                .toList();
        log.info("Start updating status for {} claims", claims.size());

        return Flux.fromIterable(claims)
                .flatMap(this::updateClaimStatus)
                .doOnError(error -> log.error("Failed to update claim status", error))
                .doOnComplete(() -> log.info("Completed updating claims - triggered by user"));
    }

    public Mono<Claim> updateClaimStatus(Claim claim) {

        if (!StringUtils.hasText(claim.getInvoiceUri())) {
            log.warn("Claim {} has no invoice URI", claim.getOrderNumber());
            return Mono.empty();
        }

        return restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri())
                .flatMap(this::updateClaimStatusAndDates)
                .onErrorResume(WebClientResponseException.class, e -> {
                    claim.setClaimStatus(ClaimStatus.UPDATE_ERROR);
                    claim.setStatusMessage(e.getMessage());
                    log.error("Error updating claim {}: [{}] {}", claim.getOrderNumber(), e.getStatusCode(), e.getMessage());
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.warn("Error updating claim {} [{}]", claim.getOrderNumber(), claim.getClaimStatus());
                    log.error("Exception: " + e.getMessage(), e);
                    return Mono.empty();
                });
    }

    private Mono<Claim> updateClaimStatusAndDates(FakturagrunnlagResource invoice) {
        Claim claim = claimRepository.get(Long.parseLong(invoice.getOrdrenummer().getIdentifikatorverdi()));
        fintClient.getSelfLink(invoice).ifPresent(claim::setInvoiceUri);

        return fintClient.getFaktura(invoice)
                .flatMap(fakturaList -> {
                    log.debug("Claim {} has {} invoices", claim.getOrderNumber(), fakturaList.size());

                    boolean hasStatusChanged = updateClaimStatus(fakturaList, claim);
                    boolean hasDateChanged = updateClaimDate(claim, fakturaList);

                    if (hasStatusChanged || hasDateChanged) {
                        claimRepository.save(claim);
                        log.info("Claim {} updated", claim.getOrderNumber());
                    } else {
                        log.debug("Claim {} has no update", claim.getOrderNumber());
                    }

                    return Mono.just(claim);
                });
    }

    private boolean updateClaimDate(Claim claim, List<FakturaResource> fakturaList) {
        Set<String> originalInvoiceNumer = claim.getInvoiceNumbers();
        LocalDate originalInvoiceDate = claim.getInvoiceDate();
        LocalDate originalPaymentDueDate = claim.getPaymentDueDate();
        Long originalAmountDue = claim.getAmountDue();

        claim.setInvoiceNumbers(fintClient.getFakturanummere(fakturaList));
        fintClient.getInvoiceDate(fakturaList).ifPresent(claim::setInvoiceDate);
        fintClient.getPaymentDueDate(fakturaList).ifPresent(claim::setPaymentDueDate);
        fintClient.getAmountDue(fakturaList).ifPresent(claim::setAmountDue);

        return !Objects.equals(originalInvoiceNumer, claim.getInvoiceNumbers()) ||
                !Objects.equals(originalInvoiceDate, claim.getInvoiceDate()) ||
                !Objects.equals(originalPaymentDueDate, claim.getPaymentDueDate()) ||
                !Objects.equals(originalAmountDue, claim.getAmountDue());
    }

    private boolean updateClaimStatus(List<FakturaResource> fakturaList, Claim claim) {
        ClaimStatus originalStatus = claim.getClaimStatus();
        boolean allCredited = fakturaList.stream().allMatch(FakturaResource::getKreditert);
        boolean allPaid = fakturaList.stream().allMatch(FakturaResource::getBetalt);
        boolean anyIssued = !fakturaList.isEmpty();

        log.debug("Claim {} has status: credited: {}, paid: {}, issued: {}", claim.getOrderNumber(), allCredited, allPaid, anyIssued);

        ClaimStatus newStatus = ClaimStatus.ACCEPTED;

        if (fakturaList.isEmpty()) {
            newStatus = ClaimStatus.ACCEPTED;
        } else if (allCredited) {
            newStatus = ClaimStatus.CREDITED;
        } else if (allPaid) {
            newStatus = ClaimStatus.PAID;
        } else if (anyIssued) {
            newStatus = ClaimStatus.ISSUED;
        }

        claim.setClaimStatus(newStatus);
        return originalStatus != newStatus;
    }

    private boolean isNewerThan(Claim claim, Duration maxAge) {
        return maxAge == null || Duration.between(claim.getCreatedDate(), LocalDateTime.now()).compareTo(maxAge) <= 0;
    }
}
