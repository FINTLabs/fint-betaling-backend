package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ClaimRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String ORG_ID = "orgId";

    @Value("${fint.betaling.org-id}")
    private String orgId;

    private final AtomicLong orderNumberCounter = new AtomicLong(100000L);

    public Claim storeClaim(Claim claim) {
        claim.setOrderNumber(String.valueOf(orderNumberCounter.incrementAndGet()));
        mongoTemplate.save(claim);
        return claim;
    }

    public List<Claim> getClaims(Query query) {
        return mongoTemplate.find(query, Claim.class);
    }

    public void updateClaim(Query query, Update update) {
        mongoTemplate.upsert(query, update, Claim.class);
    }

    @PostConstruct
    public void setHighestOrderNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));

        StreamUtils.createStreamFromIterator(mongoTemplate.stream(query, Claim.class))
                .map(Claim::getOrderNumber)
                .mapToLong(Long::parseLong)
                .max()
                .ifPresent(orderNumberCounter::set);
    }
}
