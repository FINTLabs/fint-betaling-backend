package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.vocab.BetalingStatus;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import org.jooq.lambda.function.Consumer2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvoiceRepository {

    private static final String SENDT_TIL_EKSTERNT_SYSTEM = "sentTilEksterntSystem";
    private static final String ORDRENUMMER = "ordrenummer";
    private static final String LOCATION = "location";
    private static final String STATUS = "status";
    private static final String ERROR = "error";

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private MongoRepository mongoRepository;

    /*
    public void sendInvoices(String orgId) {
        List<Betaling> payments = getUnsentPayments(orgId);
        for (Betaling payment : payments) {
            ResponseEntity response = setInvoice(orgId, payment.getFakturagrunnlag());
            payment.setLocation(response.getHeaders().getLocation().toString());
            updatePaymentLocation(orgId, payment);
        }
    }
     */

    public List<Betaling> sendInvoices(String orgId, List<Long> ordrenummer) {
        List<Betaling> unsentPayments = getUnsentPayments(orgId).stream()
                .filter(b -> ordrenummer.contains(b.getOrdrenummer()))
                .collect(Collectors.toList());

        List<Betaling> sentPayments = new ArrayList<>();

        unsentPayments.forEach(payment -> {
            try {
                ResponseEntity responseEntity = setInvoice(orgId, payment.getFakturagrunnlag());
                payment.setLocation(responseEntity.getHeaders().getLocation().toString());
                payment.setSentTilEksterntSystem(true);
                payment.setStatus(BetalingStatus.SENT);
                payment.setError(null);
                updatePaymentOnSuccess(orgId, payment);
            } catch (InvalidResponseException ex) {
                payment.setStatus(BetalingStatus.ERROR);
                payment.setError(ex.getLocalizedMessage());
                updatePaymentOnError(orgId, payment);
            }
            sentPayments.add(payment);
        });

        return sentPayments;
    }

    public void updateInvoiceStatus(String orgId) {
        getSentPayments(orgId).forEach(Consumer2.from(this::getPaymentStatus).acceptPartially(orgId));
    }

    private void getPaymentStatus(String orgId, Betaling payment) {
        try {
            updateInvoice(orgId, getStatus(orgId, payment));
            log.info("Updated {}", payment.getOrdrenummer());
        } catch (Exception e) {
            log.warn("Error updating {}: {}", payment.getOrdrenummer(), e.getMessage());
        }
    }

    private List<Betaling> getUnsentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where(SENDT_TIL_EKSTERNT_SYSTEM).is(false));
        return getPayments(orgId, query);
    }

    private List<Betaling> getSentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where(SENDT_TIL_EKSTERNT_SYSTEM).is(true));
        return getPayments(orgId, query);
    }

    private void updatePaymentOnSuccess(String orgId, Betaling payment) {
        Update update = new Update();
        update.set(LOCATION, payment.getLocation());
        update.set(STATUS, BetalingStatus.SENT);
        update.set(SENDT_TIL_EKSTERNT_SYSTEM, true);
        update.unset(ERROR);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where(ORDRENUMMER).is(payment.getOrdrenummer()));
        updatePayment(orgId, query, update);
    }

    private void updatePaymentOnError(String orgId, Betaling payment) {
        Update update = new Update();
        update.set(STATUS, BetalingStatus.ERROR);
        update.set(ERROR, payment.getError());

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where(ORDRENUMMER).is(payment.getOrdrenummer()));
        updatePayment(orgId, query, update);
    }

    public ResponseEntity setInvoice(String orgId, FakturagrunnlagResource invoice) {
        return restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice, orgId);
    }

    public FakturagrunnlagResource getStatus(String orgId, Betaling payment) {
        return restUtil.get(FakturagrunnlagResource.class, payment.getLocation(), orgId);
    }

    public void updateInvoice(String orgId, FakturagrunnlagResource invoice) {
        Update update = new Update();
        Consumer2<String, Object> updater = Consumer2.from(update::set);
        updater.accept("fakturagrunnlag", invoice);
        invoice.getSelfLinks().stream().map(Link::getHref).findAny().ifPresent(updater.acceptPartially(LOCATION));
        Optional.ofNullable(invoice.getFakturanummer()).map(Identifikator::getIdentifikatorverdi).map(Long::valueOf).ifPresent(updater.acceptPartially("fakturanummer"));
        Optional.ofNullable(invoice.getTotal()).map(String::valueOf).ifPresent(updater.acceptPartially("restBelop"));

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where(ORDRENUMMER).is(Long.valueOf(invoice.getOrdrenummer().getIdentifikatorverdi())));

        mongoRepository.updatePayment(orgId, query, update);
    }

    public List<Betaling> getPayments(String orgId, Query query) {
        return mongoRepository.getPayments(orgId, query);
    }

    public void updatePayment(String orgId, Query query, Update update) {
        mongoRepository.updatePayment(orgId, query, update);
    }
}