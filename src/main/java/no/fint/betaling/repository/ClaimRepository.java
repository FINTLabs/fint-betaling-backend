package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClaimRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String ORG_ID = "orgId";

    @Value("${fint.betaling.org-id}")
    private String orgId;

    public void storeClaim(Claim claim) {
        mongoTemplate.save(claim);
    }

    public List<Claim> getClaims(Query query) {
        return mongoTemplate.find(query, Claim.class);
    }

    public void updateClaim(Query query, Update update) {
        mongoTemplate.upsert(query, update, Claim.class);
    }

    public Long getHighestOrderNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));

        List<Claim> claims = mongoTemplate.find(query, Claim.class);

        return claims.stream().map(Claim::getOrderNumber).mapToLong(Long::parseLong).max().orElse(100000L);
    }
}
