package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class OrderNumberRepository {

    private static final String ORG_ID = "orgId";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    public Long getHighestOrderNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));

        List<Claim> claims = mongoTemplate.find(query, Claim.class);

        return claims.stream().map(Claim::getOrderNumber).mapToLong(Long::parseLong).max().orElse(100000L);
    }
}
