package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.BetalingFactory;
import no.fint.betaling.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private BetalingFactory betalingFactory;

    @Autowired
    private OrderNumberService orderNumberService;

    public List<Betaling> getAllPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        return mongoService.getPayments(orgId, query);
    }

    public List<Betaling> getPaymentsByCustomerName(String orgId, String name) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("kunde.navn").regex(name, "i"));
        return mongoService.getPayments(orgId, query);
    }

    public List<Betaling> getPaymentsByOrdernumber(String orgId, String ordernumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.Betaling"));
        query.addCriteria(Criteria.where("ordrenummer").is(Long.parseLong(ordernumber)));
        return mongoService.getPayments(orgId, query);
    }

    public List<Betaling> setPayment(String orgId, Payment payment) {
        List<Betaling> payments = betalingFactory.getBetaling(payment, orgId);
        payments.forEach(p -> {
            mongoService.setPayment(orgId, p);
        });
        return payments;
    }
}
