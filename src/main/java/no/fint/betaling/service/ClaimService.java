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
                        URI invoiceUri = sendClaim(InvoiceFactory.createInvoice(claim));
                        claim.setInvoiceUri(invoiceUri);
                        claim.setClaimStatus(ClaimStatus.SENT);
                        claim.setStatusMessage(null);
                    } catch (InvalidResponseException e) {
                        claim.setClaimStatus(ClaimStatus.SEND_ERROR);
                        claim.setStatusMessage(e.getMessage());
                    }
                    updateClaimStatus(claim);
                })
                .collect(Collectors.toList());
    }

    public URI sendClaim(FakturagrunnlagResource invoice) {
        ResponseEntity<FakturagrunnlagResource> responseEntity =
                restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice);
        return responseEntity.getHeaders().getLocation();
    }

    void updateClaims() {
        getSentClaims().forEach(claim -> {
            try {
                FakturagrunnlagResource resource = restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri());
                updateClaim(resource);
                claim.setStatusMessage(null);
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.UPDATE_ERROR);
                claim.setStatusMessage(e.getMessage());
            }
            updateClaimStatus(claim);
        });
    }

    /**
     * TODO Needs to update with both {@link FakturagrunnlagResource} and {@link FakturaResource}
     */
    public void updateClaim(FakturagrunnlagResource invoice) {
        Update update = new Update();
        Consumer2<String, Object> updater = Consumer2.from(update::set);
        //updater.accept("fakturagrunnlag", invoice);
        invoice.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findAny()
                .ifPresent(updater.acceptPartially(INVOICE_URI));
        Optional.ofNullable(invoice.getOrdrenummer())
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

    private void updateClaimStatus(Claim claim) {
        Update update = new Update();
        update.set(INVOICE_URI, claim.getInvoiceUri());
        update.set(CLAIM_STATUS, claim.getClaimStatus());
        update.set(STATUS_MESSAGE, claim.getStatusMessage());

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(claim.getOrderNumber()));
        claimRepository.updateClaim(query, update);
    }

    public List<Claim> getClaims() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        return claimRepository.getClaims(query);
    }

    private List<Claim> getSentClaims() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId).orOperator(
                Criteria.where(CLAIM_STATUS).is(ClaimStatus.SENT),
                Criteria.where(CLAIM_STATUS).is(ClaimStatus.UPDATE_ERROR)));
        return claimRepository.getClaims(query);
    }

    private List<Claim> getUnsentClaims() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId).orOperator(
                Criteria.where(CLAIM_STATUS).is(ClaimStatus.STORED),
                Criteria.where(CLAIM_STATUS).is(ClaimStatus.SEND_ERROR)));
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
}