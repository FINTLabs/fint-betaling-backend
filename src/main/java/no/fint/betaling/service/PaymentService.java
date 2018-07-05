package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.Kunde;
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;
import no.fint.model.administrasjon.okonomi.Fakturalinje;
import no.fint.model.administrasjon.okonomi.Varelinje;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private OrderNumberService orderNumberService;

    public List<Betaling> getAllPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        return mongoService.getPayments(orgId, query);
    }

    public List<Betaling> getPaymentsByLastname(String orgId, String lastName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("kunde.navn.etternavn").regex(lastName, "i"));
        return mongoService.getPayments(orgId, query);
    }

    public List<Betaling> getPaymentsByOrdernumber(String orgId, String ordernumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("ordrenummer").regex(
                orderNumberService.getOrderNumberFromNumber(orgId, ordernumber),
                "i")
        );
        return mongoService.getPayments(orgId, query);
    }

    public void setPayment(String orgId, List<VarelinjeResource> orderLines, List<Kunde> customers) {
        customers.forEach(customer -> {
            Betaling payment = new Betaling();
            payment.setOrdrenummer(orderNumberService.getOrderNumber(orgId));
            payment.setKunde(customer);
            payment.setVarelinjer(orderLines);
            mongoService.setPayment(orgId, payment);
        });
    }
}
