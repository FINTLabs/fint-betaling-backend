package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClaimRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void setClaim(Claim claim) {
        mongoTemplate.save(claim);
    }

    public List<Claim> getClaims(Query query) {
        return mongoTemplate.find(query, Claim.class);
    }

    public void updateClaim(Query query, Update update) {
        mongoTemplate.upsert(query, update, Claim.class);
    }
}
