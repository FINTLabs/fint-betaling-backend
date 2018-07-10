package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
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

@Service
public class InvoiceService {

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    @Autowired
    private RestService restService;

    @Autowired
    private MongoService mongoService;

    public List<FakturagrunnlagResource> getInvoices(String orgId) {
        return restService.getResource(FakturagrunnlagResources.class, invoiceEndpoint, orgId).getContent();
    }

    public ResponseEntity setInvoice(String orgId, FakturagrunnlagResource invoice) {
        return restService.setResource(FakturagrunnlagResource.class, invoiceEndpoint, invoice, orgId);
    }

    public FakturagrunnlagResource getStatus(String orgId, Betaling payment) {
        return restService.getResource(FakturagrunnlagResource.class, payment.getLocation().toString(), orgId);
    }

    public void updateInvoice(String orgId, FakturagrunnlagResource invoice) {
        Update update = new Update();
        update.set("fakturagrunnlag", invoice);

        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("ordrenummer").is(invoice.getOrdrenummer().getIdentifikatorverdi()));

        mongoService.updatePayment(orgId, query, update);
    }
}