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

    @Value("${fint.betaling.endpoints.invoice}")
    private URI invoiceEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimFactory claimFactory;

    public List<Claim> sendClaims(String orgId, List<String> orderNumbers) {
        List<Claim> unsentClaims = getUnsentClaims(orgId).stream()
                .filter(b -> orderNumbers.contains(b.getOrderNumber()))
                .collect(Collectors.toList());

        List<Claim> sentClaims = new ArrayList<>();

        unsentClaims.forEach(claim -> {
            try {
                URI invoiceUri = submitClaim(orgId, InvoiceFactory.createInvoice(claim));
                claim.setInvoiceUri(invoiceUri);
                claim.setClaimStatus(ClaimStatus.SENT);
                updateClaimOnSuccess(orgId, claim);
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.ERROR);
                claim.setStatusMessage(e.getMessage());
                updateClaimOnError(orgId, claim);
            }
            sentClaims.add(claim);
        });

        return sentClaims;
    }

    public void updateClaimStatus(String orgId) {
        getSentClaims(orgId).forEach(Consumer2.from(this::getClaimStatus).acceptPartially(orgId));
    }

    private void getClaimStatus(String orgId, Claim claim) {
        try {
            updateClaim(orgId, getStatus(orgId, claim));
            log.info("Updated {}", claim.getOrderNumber());
        } catch (InvalidResponseException e) {
            claim.setClaimStatus(ClaimStatus.ERROR);
            claim.setStatusMessage(e.getMessage());
            updateClaimOnError(orgId, claim);
        } catch (Exception e) {
            log.warn("Error updating {}: {}", claim.getOrderNumber(), e.getMessage());
        }
    }

    private List<Claim> getUnsentClaims(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(CLAIM_STATUS).is(ClaimStatus.STORED).orOperator(Criteria.where(CLAIM_STATUS)).is(ClaimStatus.ERROR));
        return getClaims(orgId, query);
    }

    private List<Claim> getSentClaims(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(CLAIM_STATUS).is(ClaimStatus.SENT));
        return getClaims(orgId, query);
    }

    private void updateClaimOnSuccess(String orgId, Claim claim) {
        Update update = new Update();
        update.set(INVOICE_URI, claim.getInvoiceUri());
        update.set(CLAIM_STATUS, ClaimStatus.SENT);
        update.unset(STATUS_MESSAGE);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(claim.getOrderNumber()));
        updateClaim(orgId, query, update);
    }

    private void updateClaimOnError(String orgId, Claim claim) {
        Update update = new Update();
        update.set(CLAIM_STATUS, ClaimStatus.ERROR);
        update.set(STATUS_MESSAGE, claim.getStatusMessage());

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(claim.getOrderNumber()));
        updateClaim(orgId, query, update);
    }

    public URI submitClaim(String orgId, FakturagrunnlagResource invoice) {
        ResponseEntity<FakturagrunnlagResource> responseEntity =
                restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice, orgId);
        return responseEntity.getHeaders().getLocation();
    }

    public FakturagrunnlagResource getStatus(String orgId, Claim claim) {
        return restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri(), orgId);
    }

    public void updateClaim(String orgId, FakturagrunnlagResource invoice) {
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
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(Long.valueOf(invoice.getOrdrenummer().getIdentifikatorverdi())));

        claimRepository.updateClaim(orgId, query, update);
    }

    public List<Claim> getClaims(String orgId, Query query) {
        return claimRepository.getClaims(orgId, query);
    }

    public void updateClaim(String orgId, Query query, Update update) {
        claimRepository.updateClaim(orgId, query, update);
    }

    public List<Claim> getAllClaims(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        return claimRepository.getClaims(orgId, query);
    }

    public List<Claim> getClaimsByCustomerName(String orgId, String name) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(CUSTOMER_NAME).regex(name, "i"));
        return claimRepository.getClaims(orgId, query);
    }

    public List<Claim> getClaimsByOrderNumber(String orgId, String orderNumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(Long.parseLong(orderNumber)));
        return claimRepository.getClaims(orgId, query);
    }

    public List<Claim> setClaim(String orgId, Order order) {
        List<Claim> claims = claimFactory.createClaim(order, orgId);
        claims.forEach(p -> claimRepository.setClaim(orgId, p));
        return claims;
    }
}