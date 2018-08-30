package no.fint.betaling.repository;

import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.BetalingFactory;
import no.fint.betaling.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentRepository {

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private BetalingFactory betalingFactory;

    public List<Betaling> getAllPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        return mongoRepository.getPayments(orgId, query);
    }

    public List<Betaling> getPaymentsByCustomerName(String orgId, String name) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where("kunde.navn").regex(name, "i"));
        return mongoRepository.getPayments(orgId, query);
    }

    public List<Betaling> getPaymentsByOrdernumber(String orgId, String ordernumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Betaling.class.getName()));
        query.addCriteria(Criteria.where("ordrenummer").is(Long.parseLong(ordernumber)));
        return mongoRepository.getPayments(orgId, query);
    }

    public List<Betaling> setPayment(String orgId, Payment payment) {
        List<Betaling> payments = betalingFactory.getBetaling(payment, orgId);
        payments.forEach(p -> mongoRepository.setPayment(orgId, p));
        return payments;
    }
}
