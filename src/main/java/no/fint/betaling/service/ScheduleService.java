package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.InvoiceFactory;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceFactory invoiceFactory;

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    public void sendInvoices(String orgId) {
        List<Betaling> payments = getUnsentPayments(orgId);
        for (Betaling payment : payments){
            FakturagrunnlagResource invoice = invoiceFactory.getInvoice(payment);
            ResponseEntity response = invoiceService.setInvoice(orgId, invoice);
            payment.setLocation(response.getHeaders().getLocation());
            updatePaymentLocation(orgId, payment);
        }
    }

    private List<Betaling> getUnsentPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sentTilEksterntSystem").is(false));
        return mongoService.getPayments(orgId, query);
    }

    private void updatePaymentLocation(String orgId, Betaling payment) {
        Update update = new Update();
        update.set("location", payment.getLocation());

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("ordrenummer").is(payment.getOrdrenummer()));
        mongoService.updatePayment(orgId, query, update);
    }
}
