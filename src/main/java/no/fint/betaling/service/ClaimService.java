package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.factory.ClaimFactory;
import no.fint.betaling.factory.InvoiceFactory;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimsDatePeriod;
import no.fint.betaling.model.Order;
import no.fint.betaling.repository.ClaimRepository;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.function.Consumer2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimService {

    private static final String INVOICE_URI = "invoiceUri";
    private static final String AMOUNT_DUE = "amountDue";
    private static final String CLAIM_STATUS = "claimStatus";
    private static final String STATUS_MESSAGE = "statusMessage";

    @Value("${fint.betaling.endpoints.invoice:/okonomi/faktura/fakturagrunnlag}")
    private String invoiceEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimFactory claimFactory;

    @Autowired
    private InvoiceFactory invoiceFactory;

    @Autowired
    private QueryService queryService;

    public List<Claim> storeClaims(Order order) {
        return claimFactory
                .createClaims(order)
                .stream()
                .map(claimRepository::storeClaim)
                .collect(Collectors.toList());
    }

    public Flux<Claim> sendClaims(List<String> orderNumbers) {
        return Flux.fromIterable(getUnsentClaims())
                .filter(claim -> orderNumbers.contains(claim.getOrderNumber()))
                .flatMap(this::checkClaimStatus)
                .onErrorResume(throwable -> {
                    log.error("Error occurred while sending claims", throwable);
                    return Flux.error(throwable);
                });
    }

    private Mono<Claim> checkClaimStatus(Claim claim) {
        FakturagrunnlagResource invoice = invoiceFactory.createInvoice(claim);

        return restUtil.post(invoiceEndpoint, invoice, FakturagrunnlagResource.class, claim.getOrgId())
                .doOnNext(httpHeaders -> {
                    if (httpHeaders.getLocation() != null) {
                        claim.setInvoiceUri(httpHeaders.getLocation().toString());
                        claim.setClaimStatus(ClaimStatus.SENT);
                        claim.setStatusMessage(null);
                        log.info("Claim {} sent, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                        updateClaimStatus(claim);
                    }
                })
                .doOnError(WebClientResponseException.class, ex -> {
                    claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                    claim.setStatusMessage(ex.getMessage());
                    log.error("Error sending claim {}: {}", claim.getOrderNumber(), ex.getStatusCode());
                    updateClaimStatus(claim);
                })
                .thenReturn(claim);
    }

    public void updateSentClaims() {
        getSentClaims().forEach(claim -> {
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
                        updateClaimStatus(claim);
                    })
                    .doOnError(WebClientResponseException.class, e -> {
                        if (e.getStatusCode() == HttpStatus.GONE) {
                            log.info("Claim {} gone from consumer -- retry sending!", claim.getOrderNumber());
                            claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                            claim.setInvoiceUri(null);
                            updateClaimStatus(claim);
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
                    updateClaimStatus(claim);
                    log.warn("Error accepting claim {} [{}]: [{}] {}", claim.getOrderNumber(), claim.getClaimStatus(), e2.getStatusCode(), e2.getMessage());
                })
                .onErrorResume(e -> Mono.empty()) // Håndterer andre feil og fortsetter
                .subscribe();
    }

    public void updateAcceptedClaims() {
        // TODO Accepted claims should be checked less often
        getAcceptedClaims().forEach(claim -> {

            restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri())
                    .doOnNext(fakturagrunnlagResource -> {
                        ClaimStatus newStatus = updateClaim(fakturagrunnlagResource);
                        claim.setClaimStatus(newStatus);
                        claim.setStatusMessage(null);
                        log.info("Claim {} updated", claim.getOrderNumber());
                        updateClaimStatus(claim);
                    })
                    .doOnError(WebClientResponseException.class, e -> {
                        claim.setClaimStatus(ClaimStatus.UPDATE_ERROR);
                        claim.setStatusMessage(e.getMessage());
                        log.error("Error updating claim {}: [{}] {}", claim.getOrderNumber(), e.getStatusCode(), e.getMessage());
                        updateClaimStatus(claim);
                    })
                    .doOnError(e -> {
                        log.warn("Error updating claim {} [{}]", claim.getOrderNumber(), claim.getClaimStatus());
                        log.error("Exception: " + e.getMessage(), e);
                    });
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
    public ClaimStatus updateClaim(FakturagrunnlagResource invoice) {
        Update update = new Update();
        Consumer2<String, Object> updater = Consumer2.from(update::set);

        invoice.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findAny()
                .ifPresent(updater.acceptPartially(INVOICE_URI));

        List<FakturaResource> fakturaList = invoice.getFaktura()
                .stream()
                .map(Link::getHref)
                .map(uri -> restUtil.get(FakturaResource.class, uri).block())
                .toList();

        updater.accept("invoiceNumbers", fakturaList.stream()
                .map(FakturaResource::getFakturanummer)
                .map(Identifikator::getIdentifikatorverdi)
                .collect(Collectors.toList()));

        fakturaList.stream()
                .map(FakturaResource::getDato)
                .min(Comparator.naturalOrder())
                .ifPresent(updater.acceptPartially("invoiceDate"));

        fakturaList.stream()
                .map(FakturaResource::getForfallsdato)
                .max(Comparator.naturalOrder())
                .ifPresent(updater.acceptPartially("paymentDueDate"));

        fakturaList.stream()
                .map(FakturaResource::getRestbelop)
                .filter(Objects::nonNull)
                .reduce(Long::sum)
                .ifPresent(updater.acceptPartially(AMOUNT_DUE));

        boolean credited = fakturaList.stream().allMatch(FakturaResource::getKreditert);
        boolean paid = fakturaList.stream().allMatch(FakturaResource::getBetalt);
        boolean issued = fakturaList.stream().allMatch(FakturaResource::getFakturert);

        Query query = queryService.queryByOrderNumber(invoice.getOrdrenummer().getIdentifikatorverdi());

        claimRepository.updateClaim(query, update);

        if (fakturaList.isEmpty()) {
            return ClaimStatus.ACCEPTED;
        } else if (credited || paid) {
            return ClaimStatus.PAID;
        } else if (issued) {
            return ClaimStatus.ISSUED;
        }
        return ClaimStatus.ACCEPTED;
    }

    private void updateClaimStatus(Claim claim) {
        Update update = new Update();
        update.set(INVOICE_URI, claim.getInvoiceUri());
        update.set(CLAIM_STATUS, claim.getClaimStatus());
        update.set(STATUS_MESSAGE, claim.getStatusMessage());

        Query query = queryService.queryByOrderNumber(claim.getOrderNumber());
        claimRepository.updateClaim(query, update);
    }

    public List<Claim> getClaims() {
        return claimRepository.getClaims(queryService.createQuery());
    }

    private List<Claim> getAcceptedClaims() {
        return claimRepository.getClaims(queryService.queryByClaimStatus(
                ClaimStatus.ACCEPTED,
                ClaimStatus.ISSUED,
                // ClaimStatus.PAID,  // TODO Used to be workaround for issue with Visma Fakturering
                ClaimStatus.UPDATE_ERROR));
    }

    private List<Claim> getSentClaims() {
        return claimRepository.getClaims(queryService.queryByClaimStatus(
                ClaimStatus.SENT));
    }

    private List<Claim> getUnsentClaims() {
        List<Claim> claims = claimRepository.getClaims(queryService.queryByClaimStatus(
                ClaimStatus.STORED,
                ClaimStatus.SEND_ERROR));
        return claims != null ? claims : Collections.emptyList();
    }

    public List<Claim> getClaimsByCustomerName(String name) {
        return claimRepository.getClaims(queryService.queryByCustomerNameRegex(name));
    }

    public List<Claim> getClaimsByOrderNumber(String orderNumber) {
        return claimRepository.getClaims(queryService.queryByOrderNumber(orderNumber));
    }

    public List<Claim> getClaimsByStatus(ClaimStatus[] statuses) {
        return claimRepository.getClaims(queryService.queryByClaimStatus(statuses));
    }

    public int countClaimsByStatus(ClaimStatus[] statuses, String days) {
        if (StringUtils.isNotBlank(days)) {
            return claimRepository.countClaims(queryService.queryByClaimStatusByDays(Long.parseLong(days), statuses));
        }
        return claimRepository.countClaims(queryService.queryByClaimStatus(statuses));
    }

    public void cancelClaim(String orderNumber) {
        List<Claim> claimsByOrderNumber = getClaimsByOrderNumber(orderNumber);
        claimsByOrderNumber
                .stream()
                .filter(claim -> claim.getClaimStatus().equals(ClaimStatus.STORED))
                .peek(claim -> claim.setClaimStatus(ClaimStatus.CANCELLED))
                .forEach(this::updateClaimStatus);
    }

    public List<Claim> getClaims(ClaimsDatePeriod period, String organisationNumber, ClaimStatus[] statuses) {

        return claimRepository.getClaims(
                queryService.queryByDateAndSchoolAndStatus(
                        claimsDatePeriodToTimestamp(period),
                        organisationNumber,
                        statuses)
        );
    }

    private Date claimsDatePeriodToTimestamp(ClaimsDatePeriod period) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        switch (period) {
            case ALL:
                break;
            case WEEK:
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                break;
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case YEAR:
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }

        return period == ClaimsDatePeriod.ALL ? null : calendar.getTime();
    }
}