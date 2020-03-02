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
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.FakturaResource;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import org.jooq.lambda.function.Consumer2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
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
                        ResponseEntity<?> responseEntity = restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice);
                        claim.setInvoiceUri(responseEntity.getHeaders().getLocation().toString());
                        claim.setClaimStatus(ClaimStatus.SENT);
                        claim.setStatusMessage(null);
                        log.info("Claim {} sent, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                    } catch (InvalidResponseException e) {
                        claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                        claim.setStatusMessage(e.getMessage());
                        log.error("Error sending claim {}: {}", claim.getOrderNumber(), e.getStatus(), e);
                    }
                    updateClaimStatus(claim);
                })
                .collect(Collectors.toList());
    }

    void updateClaims() {
        getSentClaims().forEach(claim -> {
            try {
                ResponseEntity<?> responseEntity = restUtil.get(ResponseEntity.class, claim.getInvoiceUri());
                if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                    claim.setInvoiceUri(responseEntity.getHeaders().getLocation().toString());
                    claim.setClaimStatus(ClaimStatus.ACCEPTED);
                    log.info("Claim {} accepted, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                } else {
                    claim.setClaimStatus(ClaimStatus.SENT);
                    log.info("Claim {} pending, location: {}", claim.getOrderNumber(), claim.getInvoiceUri());
                }
                claim.setStatusMessage(null);
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.ACCEPT_ERROR);
                claim.setStatusMessage(e.getMessage());
                log.error("Error accepting claim {}: {}", claim.getOrderNumber(), e.getStatus(), e);
            }
            updateClaimStatus(claim);
        });

        getAcceptedClaims().forEach(claim -> {
            try {
                FakturagrunnlagResource invoice = restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri());
                updateClaim(invoice);
                claim.setStatusMessage(null);
                log.info("Claim {} updated", claim.getOrderNumber());
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.UPDATE_ERROR);
                claim.setStatusMessage(e.getMessage());
                log.error("Error updating claim {}: {}", claim.getOrderNumber(), e.getStatus(), e);
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
     */
    public void updateClaim(FakturagrunnlagResource invoice) {
        Update update = new Update();
        Consumer2<String, Object> updater = Consumer2.from(update::set);

        invoice.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findAny()
                .ifPresent(updater.acceptPartially(INVOICE_URI));

        /*
        Optional.ofNullable(invoice.getOrdrenummer())
                .map(Identifikator::getIdentifikatorverdi)
                .map(Long::valueOf)
                .ifPresent(updater.acceptPartially(INVOICE_NUMBER));
         */

        Optional.ofNullable(invoice.getTotal())
                .map(String::valueOf)
                .ifPresent(updater.acceptPartially(AMOUNT_DUE));

        Query query = queryService.queryByOrderNumber(invoice.getOrdrenummer().getIdentifikatorverdi());

        claimRepository.updateClaim(query, update);
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
                ClaimStatus.UPDATE_ERROR));
    }

    private List<Claim> getSentClaims() {
        return claimRepository.getClaims(queryService.queryByClaimStatus(
                ClaimStatus.SENT,
                ClaimStatus.ACCEPT_ERROR));
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
}