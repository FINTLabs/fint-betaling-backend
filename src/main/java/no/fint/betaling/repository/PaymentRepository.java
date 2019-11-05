package no.fint.betaling.repository;

import no.fint.betaling.factory.ClaimFactory;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.Order;
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
    private ClaimFactory claimFactory;

    public List<Claim> getAllPayments(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        return mongoRepository.getPayments(orgId, query);
    }

    public List<Claim> getPaymentsByCustomerName(String orgId, String name) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where("kunde.navn").regex(name, "i"));
        return mongoRepository.getPayments(orgId, query);
    }

    public List<Claim> getPaymentsByOrdernumber(String orgId, String ordernumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where("ordrenummer").is(Long.parseLong(ordernumber)));
        return mongoRepository.getPayments(orgId, query);
    }

    public List<Claim> setPayment(String orgId, Order order) {
        List<Claim> payments = claimFactory.createClaim(order, orgId);
        payments.forEach(p -> mongoRepository.setPayment(orgId, p));
        return payments;
    }
}
