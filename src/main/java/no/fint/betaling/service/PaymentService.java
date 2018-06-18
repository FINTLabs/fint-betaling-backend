package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private MongoService mongoService;

    public List<Betaling> getAllPayments(String orgId){
        Query query = new Query();
        query.addCriteria(Criteria.where(""));
        return mongoService.getFakturagrunnlag(orgId, query);
    }

    public List<Betaling> getPaymentsByLastname(String orgId, String lastName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("kunde.navn.etternavn").regex(lastName, "i"));
        return mongoService.getFakturagrunnlag(orgId, query);
    }

    public List<Betaling> getPaymentsByOrdernumber(String orgId, String ordernumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ordrenummer").regex(ordernumber, "i"));
        return mongoService.getFakturagrunnlag(orgId, query);
    }
}
