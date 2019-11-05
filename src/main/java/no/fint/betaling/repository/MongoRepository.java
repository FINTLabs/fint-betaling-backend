package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO this class should be renamed
@Repository
public class MongoRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void setPayment(String orgId, Claim payment) {
        mongoTemplate.save(payment, orgId);
    }

    public List<Claim> getPayments(String orgId, Query query) {
        return mongoTemplate.find(query, Claim.class, orgId);
    }

    public void updatePayment(String orgId, Query query, Update update) {
        mongoTemplate.upsert(query, update, orgId);
    }
}
