package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import no.fint.betaling.factory.InvoiceFactory;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
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
public class InvoiceRepository {

    private static final String CLAIM_STATUS = "claimStatus";
    private static final String ORDER_NUMBER = "orderNumber";
    private static final String INVOICE_URI = "invoiceUri";
    private static final String STATUS = "status";
    private static final String STATUS_MESSAGE = "statusMessage";

    @Value("${fint.betaling.endpoints.invoice}")
    private URI invoiceEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private MongoRepository mongoRepository;

    public List<Claim> sendInvoices(String orgId, List<String> orderNumbers) {
        List<Claim> unsentClaims = getUnsentPayments(orgId).stream()
                .filter(b -> orderNumbers.contains(b.getOrderNumber()))
                .collect(Collectors.toList());

        List<Claim> sentPayments = new ArrayList<>();

        unsentClaims.forEach(claim -> {
            try {
                URI invoiceUri = submitClaim(orgId, InvoiceFactory.createInvoice(claim));
                claim.setInvoiceUri(invoiceUri);
                claim.setClaimStatus(ClaimStatus.SENT);
                updatePaymentOnSuccess(orgId, claim);
            } catch (InvalidResponseException e) {
                claim.setClaimStatus(ClaimStatus.ERROR);
                claim.setStatusMessage(e.getMessage());
                updatePaymentOnError(orgId, claim);
            }
            sentPayments.add(claim);
        });

        return sentPayments;
    }

    public void updateInvoiceStatus(String orgId) {
        getSentPayments(orgId).forEach(Consumer2.from(this::getPaymentStatus).acceptPartially(orgId));
    }

    private void getPaymentStatus(String orgId, Claim claim) {
        try {
            updateInvoice(orgId, getStatus(orgId, claim));
            log.info("Updated {}", claim.getOrderNumber());
        } catch (InvalidResponseException e) {
            claim.setClaimStatus(ClaimStatus.ERROR);
            claim.setStatusMessage(e.getMessage());
            updatePaymentOnError(orgId, claim);
        } catch (Exception e) {
            log.warn("Error updating {}: {}", claim.getOrderNumber(), e.getMessage());
        }
    }

    private List<Claim> getUnsentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(CLAIM_STATUS).is(false));
        return getPayments(orgId, query);
    }

    private List<Claim> getSentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(CLAIM_STATUS).is(ClaimStatus.SENT));
        return getPayments(orgId, query);
    }

    private void updatePaymentOnSuccess(String orgId, Claim payment) {
        Update update = new Update();
        update.set(INVOICE_URI, payment.getInvoiceUri());
        update.set(STATUS, ClaimStatus.SENT);
        update.set(CLAIM_STATUS, ClaimStatus.SENT);
        update.unset(STATUS_MESSAGE);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(payment.getAmountDue()));
        updatePayment(orgId, query, update);
    }

    private void updatePaymentOnError(String orgId, Claim claim) {
        Update update = new Update();
        update.set(STATUS, ClaimStatus.ERROR);
        update.set(STATUS_MESSAGE, claim.getStatusMessage());

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORDER_NUMBER).is(claim.getOrderNumber()));
        updatePayment(orgId, query, update);
    }

    public URI submitClaim(String orgId, FakturagrunnlagResource invoice) {
        ResponseEntity<FakturagrunnlagResource> responseEntity =
                restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice, orgId);
        return responseEntity.getHeaders().getLocation();
    }

    public FakturagrunnlagResource getStatus(String orgId, Claim claim) {
        return restUtil.get(FakturagrunnlagResource.class, claim.getInvoiceUri(), orgId);
    }

    public void updateInvoice(String orgId, FakturagrunnlagResource invoice) {
        Update update = new Update();
        Consumer2<String, Object> updater = Consumer2.from(update::set);
        updater.accept("fakturagrunnlag", invoice);
        invoice.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findAny()
                .ifPresent(updater.acceptPartially(INVOICE_URI));
        Optional.ofNullable(invoice.getFakturanummer())
                .map(Identifikator::getIdentifikatorverdi)
                .map(Long::valueOf)
                .ifPresent(updater.acceptPartially("invoiceNumber"));
        Optional.ofNullable(invoice.getTotal())
                .map(String::valueOf)
                .ifPresent(updater.acceptPartially("restBelop"));

        Query query = new Query();
        query.addCriteria(Criteria.where("_class")
                .is(Claim.class.getName())
        );
        query.addCriteria(Criteria
                .where(ORDER_NUMBER)
                .is(Long.valueOf(invoice.getOrdrenummer().getIdentifikatorverdi()))
        );

        mongoRepository.updatePayment(orgId, query, update);
    }

    public List<Claim> getPayments(String orgId, Query query) {
        return mongoRepository.getPayments(orgId, query);
    }

    public void updatePayment(String orgId, Query query, Update update) {
        mongoRepository.updatePayment(orgId, query, update);
    }
}