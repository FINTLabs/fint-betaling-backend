package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.InvoiceFactory;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private InvoiceService invoiceService;

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    public void sendInvoices(String orgId) {
        List<Betaling> payments = getUnsentPayments(orgId);
        for (Betaling payment : payments){
            ResponseEntity response = invoiceService.setInvoice(orgId, payment.getFakturagrunnlag());
            payment.setLocation(response.getHeaders().getLocation());
            updatePaymentLocation(orgId, payment);
        }
    }

    public void checkInvoiceStatus(String orgId){
        List<Betaling> payments = getSentPayments(orgId);
        payments.forEach(payment -> getPaymentStatus(orgId,payment));
    }

    private List<Betaling> getUnsentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sentTilEksterntSystem").is(false));
        return mongoService.getPayments(orgId, query);
    }

    private List<Betaling> getSentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sentTilEksterntSystem").is(true));
        return mongoService.getPayments(orgId, query);
    }

    private void updatePaymentLocation(String orgId, Betaling payment) {
        Update update = new Update();
        update.set("location", payment.getLocation());
        update.set("sentTilEksterntSystem", true);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("ordrenummer").is(payment.getOrdrenummer()));
        mongoService.updatePayment(orgId, query, update);
    }

    public void getPaymentStatus(String orgId, Betaling payment){
        log.info(invoiceService.getStatus(orgId, payment).toString());
    }
}
