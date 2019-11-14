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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimService {

    private static final String ORDER_NUMBER = "orderNumber";
    private static final String CUSTOMER_NAME = "customer.name";
    private static final String INVOICE_URI = "invoiceUri";
    private static final String INVOICE_NUMBER = "invoiceNumber";
    private static final String AMOUNT_DUE = "amountDue";
    private static final String CLAIM_STATUS = "claimStatus";
    private static final String STATUS_MESSAGE = "statusMessage";
    private static final String ORG_ID = "orgId";

    @Value("${fint.betaling.endpoints.invoice}")
    private URI invoiceEndpoint;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimFactory claimFactory;

    public List<Claim> sendClaims(List<String> orderNumbers) {
        List<Claim> unsentClaims = getUnsentClaims().stream()
                .filter(b -> orderNumbers.contains(b.getOrderNumber()))
                .collect(Collectors.toList());

        List<Claim> sentClaims = new ArrayList<>();

        unsentClaims.forEach(claim -> {
            try {
                URI invoiceUri = submitClaim(InvoiceFactory.createInvoice(claim));
                claim.setInvoiceUri(invoiceUri);
                claim.setClaimStatus(ClaimStatus.SENT);
                updateClaimOnSuccess(claim);
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.ERROR);
                claim.setStatusMessage(e.getMessage());
                updateClaimOnError(claim);
            }
            sentClaims.add(claim);
        });

        return sentClaims;
    }

    public void updateClaimStatus() {
        getSentClaims().forEach(this::getClaimStatus);
    }

    /** TODO Needs to update with both {@link FakturagrunnlagResource} and {@link FakturaResource} */
    private void getClaimStatus(Claim claim) {
        try {
            updateClaim(getStatus(claim));
            log.info("Updated {}", claim.getOrderNumber());
        } catch (InvalidResponseException e) {
            claim.setClaimStatus(ClaimStatus.ERROR);
            claim.setStatusMessage(e.getMessage());
            updateClaimOnError(claim);
        } catch (Exception e) {
            log.warn("Error updating {}: {}", claim.getOrderNumber(), e.getMessage());
        }
    }

    private List<Claim> getUnsentClaims() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()).orOperator(
                Criteria.where(CLAIM_STATUS).is(ClaimStatus.STORED),
                Criteria.where(CLAIM_STATUS).is(ClaimStatus.ERROR)));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        return getClaims(query);
    }

    private List<Claim> getSentClaims() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(CLAIM_STATUS).is(ClaimStatus.SENT));
        return getClaims(query);
    }

    private void updateClaimOnSuccess(Claim claim) {
        Update update = new Update();
        update.set(INVOICE_URI, claim.getInvoiceUri());
        update.set(CLAIM_STATUS, ClaimStatus.SENT);
        update.unset(STATUS_MESSAGE);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(claim.getOrderNumber()));
        updateClaim(query, update);
    }

    private void updateClaimOnError(Claim claim) {
        Update update = new Update();
        update.set(CLAIM_STATUS, ClaimStatus.ERROR);
        update.set(STATUS_MESSAGE, claim.getStatusMessage());

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(claim.getOrderNumber()));
        updateClaim(query, update);
    }

    public URI submitClaim(FakturagrunnlagResource invoice) {
        ResponseEntity<FakturagrunnlagResource> responseEntity =
                restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice);
        return responseEntity.getHeaders().getLocation();
    }

    public FakturagrunnlagResource getStatus(Claim claim) {
        return restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri());
    }

    public void updateClaim(FakturagrunnlagResource invoice) {
        Update update = new Update();
        Consumer2<String, Object> updater = Consumer2.from(update::set);
        //updater.accept("fakturagrunnlag", invoice);
        invoice.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findAny()
                .ifPresent(updater.acceptPartially(INVOICE_URI));
        Optional.ofNullable(invoice.getFakturanummer())
                .map(Identifikator::getIdentifikatorverdi)
                .map(Long::valueOf)
                .ifPresent(updater.acceptPartially(INVOICE_NUMBER));
        Optional.ofNullable(invoice.getTotal())
                .map(String::valueOf)
                .ifPresent(updater.acceptPartially(AMOUNT_DUE));

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(invoice.getOrdrenummer().getIdentifikatorverdi()));

        claimRepository.updateClaim(query, update);
    }

    public List<Claim> getClaims(Query query) {
        return claimRepository.getClaims(query);
    }

    public void updateClaim(Query query, Update update) {
        claimRepository.updateClaim(query, update);
    }

    public List<Claim> getAllClaims() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        return claimRepository.getClaims(query);
    }

    public List<Claim> getClaimsByCustomerName(String name) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(CUSTOMER_NAME).regex(name, "i"));
        return claimRepository.getClaims(query);
    }

    public List<Claim> getClaimsByOrderNumber(String orderNumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(orderNumber));
        return claimRepository.getClaims(query);
    }

    public List<Claim> setClaim(Order order) {
        List<Claim> claims = claimFactory.createClaim(order);
        claims.forEach(claim -> claimRepository.setClaim(claim));
        return claims;
    }
}