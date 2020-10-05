package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import no.fint.betaling.factory.ClaimFactory;
import no.fint.betaling.factory.InvoiceFactory;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Order;
import no.fint.betaling.repository.ClaimRepository;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.FakturaResource;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import org.jooq.lambda.function.Consumer2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimService {

    private static final String INVOICE_URI = "invoiceUri";
    //private static final String INVOICE_NUMBER = "invoiceNumber";
    private static final String AMOUNT_DUE = "amountDue";
    private static final String CLAIM_STATUS = "claimStatus";
    private static final String STATUS_MESSAGE = "statusMessage";

    @Value("${fint.betaling.endpoints.invoice}")
    private URI invoiceEndpoint;

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

    public List<Claim> sendClaims(List<String> orderNumbers) {
        return getUnsentClaims().stream()
                .filter(claim -> orderNumbers.contains(claim.getOrderNumber()))
                .peek(claim -> {
                    try {
                        FakturagrunnlagResource invoice = invoiceFactory.createInvoice(claim);
                        URI location = restUtil.post(invoiceEndpoint, invoice);
                        if (location != null) {
                            claim.setInvoiceUri(location.toString());
                            claim.setClaimStatus(ClaimStatus.SENT);
                            claim.setStatusMessage(null);
                            log.info("Claim {} sent, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                        }
                    } catch (InvalidResponseException e) {
                        claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                        claim.setStatusMessage(e.getMessage());
                        log.error("Error sending claim {}: {}", claim.getOrderNumber(), e.getStatus());
                    }
                    updateClaimStatus(claim);
                })
                .collect(Collectors.toList());
    }

    void updateClaims() {
        getSentClaims().forEach(claim -> {
            try {
                HttpHeaders headers = restUtil.head(claim.getInvoiceUri());
                if (headers.getLocation() != null) {
                    claim.setInvoiceUri(headers.getLocation().toString());
                    claim.setClaimStatus(ClaimStatus.ACCEPTED);
                    log.info("Claim {} accepted, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                } else {
                    log.info("Claim {} not updated", claim.getOrderNumber());
                }
                claim.setStatusMessage(null);
            } catch (InvalidResponseException e) {
                if (e.getStatus() == HttpStatus.GONE) {
                    log.info("Claim {} gone from consumer -- retry sending!", claim.getOrderNumber());
                    claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                    claim.setInvoiceUri(null);
                } else {
                    try {
                        String result = restUtil.get(String.class, claim.getInvoiceUri());
                        log.error("Unexpected result! {}", result);
                    } catch (InvalidResponseException e2) {
                        if (e2.getStatus().is4xxClientError()) {
                            claim.setClaimStatus(ClaimStatus.ACCEPT_ERROR);
                        } else if (e2.getStatus().is5xxServerError()) {
                            claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                        }
                        claim.setStatusMessage(e2.getMessage());
                        log.warn("Error accepting claim {} [{}]: [{}] {}", claim.getOrderNumber(), claim.getClaimStatus(), e2.getStatus(), e2.getMessage());
                    }
                }
            }
            updateClaimStatus(claim);
        });

        // TODO Accepted claims should be checked less often
        getAcceptedClaims().forEach(claim -> {
            try {
                FakturagrunnlagResource invoice = restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri());
                ClaimStatus newStatus = updateClaim(invoice);
                claim.setClaimStatus(newStatus);
                claim.setStatusMessage(null);
                log.info("Claim {} updated", claim.getOrderNumber());
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.UPDATE_ERROR);
                claim.setStatusMessage(e.getMessage());
                log.error("Error updating claim {}: [{}] {}", claim.getOrderNumber(), e.getStatus(), e.getMessage());
            }
            updateClaimStatus(claim);
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
                .map(uri -> restUtil.get(FakturaResource.class, uri))
                .collect(Collectors.toList());

        updater.accept("invoiceNumbers", fakturaList.stream()
                .map(FakturaResource::getFakturanummer)
                .map(Identifikator::getIdentifikatorverdi)
                .collect(Collectors.toList()));

        fakturaList.stream()
                .map(FakturaResource::getFakturadato)
                .min(Comparator.naturalOrder())
                .ifPresent(updater.acceptPartially("invoiceDate"));

        fakturaList.stream()
                .map(FakturaResource::getForfallsdato)
                .max(Comparator.naturalOrder())
                .ifPresent(updater.acceptPartially("paymentDueDate"));

        Optional.ofNullable(invoice.getTotal())
                .map(String::valueOf)
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
                ClaimStatus.PAID,  // TODO Workaround for issue with Visma Fakturering
                ClaimStatus.UPDATE_ERROR));
    }

    private List<Claim> getSentClaims() {
        return claimRepository.getClaims(queryService.queryByClaimStatus(
                ClaimStatus.SENT));
    }

    private List<Claim> getUnsentClaims() {
        return claimRepository.getClaims(queryService.queryByClaimStatus(
                ClaimStatus.STORED,
                ClaimStatus.SEND_ERROR));
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

    public void cancelClaim(String orderNumber) {
        List<Claim> claimsByOrderNumber = getClaimsByOrderNumber(orderNumber);
        claimsByOrderNumber.stream().filter(claim -> claim.getClaimStatus().equals(ClaimStatus.STORED)).forEach(claim -> claim.setClaimStatus(ClaimStatus.CANCELLED));
        claimsByOrderNumber.forEach(this::updateClaimStatus);
    }
}