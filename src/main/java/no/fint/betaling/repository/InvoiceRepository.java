package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Betaling;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class InvoiceRepository {

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private MongoRepository mongoRepository;

    public void sendInvoices(String orgId) {
        List<Betaling> payments = getUnsentPayments(orgId);
        for (Betaling payment : payments) {
            ResponseEntity response = setInvoice(orgId, payment.getFakturagrunnlag());
            payment.setLocation(response.getHeaders().getLocation().toString());
            updatePaymentLocation(orgId, payment);
        }
    }

    public void updateInvoiceStatus(String orgId) {
        List<Betaling> payments = getSentPayments(orgId);
        payments.forEach(payment -> getPaymentStatus(orgId, payment));
    }

    private void getPaymentStatus(String orgId, Betaling payment) {
        FakturagrunnlagResource invoice = getStatus(orgId, payment);
        if (invoice != null) {
            updateInvoice(orgId, invoice);
            log.info("Updated {}", payment.getOrdrenummer());
        }
    }

    private List<Betaling> getUnsentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where("sentTilEksterntSystem").is(false));
        return getPayments(orgId, query);
    }

    private List<Betaling> getSentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where("sentTilEksterntSystem").is(true));
        return getPayments(orgId, query);
    }

    private void updatePaymentLocation(String orgId, Betaling payment) {
        Update update = new Update();
        update.set("location", payment.getLocation());
        update.set("sentTilEksterntSystem", true);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where("ordrenummer").is(payment.getOrdrenummer()));
        updatePayment(orgId, query, update);
    }

    public List<FakturagrunnlagResource> getInvoices(String orgId) {
        return restUtil.get(FakturagrunnlagResources.class, invoiceEndpoint, orgId).getContent();
    }

    public ResponseEntity setInvoice(String orgId, FakturagrunnlagResource invoice) {
        return restUtil.post(FakturagrunnlagResource.class, invoiceEndpoint, invoice, orgId);
    }

    public FakturagrunnlagResource getStatus(String orgId, Betaling payment) {
        return restUtil.get(FakturagrunnlagResource.class, payment.getLocation(), orgId);
    }

    public void updateInvoice(String orgId, FakturagrunnlagResource invoice) {
        Update update = new Update();
        update.set("fakturagrunnlag", invoice);
        invoice.getSelfLinks().stream().map(Link::getHref).findAny().ifPresent(s -> update.set("location", s));
        Optional.ofNullable(invoice.getFakturanummer()).map(Identifikator::getIdentifikatorverdi).map(Long::valueOf).ifPresent(s -> update.set("fakturanummer", s));
        Optional.ofNullable(invoice.getTotal()).map(String::valueOf).ifPresent(s -> update.set("restBelop", s));

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where("ordrenummer").is(Long.valueOf(invoice.getOrdrenummer().getIdentifikatorverdi())));

        mongoRepository.updatePayment(orgId, query, update);
    }

    public List<Betaling> getPayments(String orgId, Query query) {
        return mongoRepository.getPayments(orgId, query);
    }

    public void updatePayment(String orgId, Query query, Update update) {
        mongoRepository.updatePayment(orgId, query, update);
    }
}