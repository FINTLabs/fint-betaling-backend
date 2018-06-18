package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Betaling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void setPayment(String orgId, Betaling payment) {
        mongoTemplate.save(payment, orgId);
    }

    public List<Betaling> getPayments(String orgId, Query query) {
        return mongoTemplate.find(query, Betaling.class, orgId);
    }

    public void updatePayment(String orgId, Query query, Update update) {
        mongoTemplate.upsert(query, update, orgId);
    }


}
