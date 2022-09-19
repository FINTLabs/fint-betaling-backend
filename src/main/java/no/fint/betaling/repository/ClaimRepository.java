package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ClaimRepository {

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    private static final String ORG_ID = "orgId";

    @Value("${fint.betaling.org-id}")
    private String orgId;

    private final AtomicLong orderNumberCounter = new AtomicLong(100000L);

    public Claim storeClaim(Claim claim) {
        claim.setOrderNumber(String.valueOf(orderNumberCounter.incrementAndGet()));
        claim.setTimestamp(System.currentTimeMillis());
        mongoTemplate.save(claim);
        return claim;
    }

    public Flux<Claim> getClaims(Query query) {
        return mongoTemplate.find(query, Claim.class);
    }

    public int countClaims(Query query) {
        return Math.toIntExact(mongoTemplate.count(query, Claim.class).block());
    }

    public void updateClaim(Query query, Update update) {
        update.set("timestamp", System.currentTimeMillis());
        mongoTemplate.upsert(query, update, Claim.class);
    }

    @PostConstruct
    public void setHighestOrderNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));

        mongoTemplate.find(query, Claim.class)
                .toStream()
                .map(Claim::getOrderNumber)
                .mapToLong(Long::parseLong)
                .max()
                .ifPresent(orderNumberCounter::set);
    }
}