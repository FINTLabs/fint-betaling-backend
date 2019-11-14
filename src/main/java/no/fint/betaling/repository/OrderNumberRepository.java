package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.OrganisationConfig;
import no.fint.betaling.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.OptionalLong;

@Slf4j
@Repository
public class OrderNumberRepository {

    private static final String NEXT_ORDER_NUMBER_FOR_ORGANISATION = "nextOrderNumberForOrganisation";
    private static final String ORG_ID = "orgId";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    @Value("${fint.betaling.collection-name}")
    private String collectionName;

    public String getOrderNumber() {
        return getAndUpdateLastOrderNumber();
    }

    private String getAndUpdateLastOrderNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));
        query.addCriteria(Criteria.where("_class").is(OrganisationConfig.class.getName()));

        Update update = new Update();
        update.inc(NEXT_ORDER_NUMBER_FOR_ORGANISATION, 1);

        FindAndModifyOptions.options().returnNew(false);
        OrganisationConfig organisationConfig =
                mongoTemplate.findAndModify(query, update, OrganisationConfig.class, collectionName);

        if (organisationConfig == null) {
            organisationConfig = new OrganisationConfig();
            organisationConfig.setOrgId(orgId);
            organisationConfig.setNextOrderNumberForOrganisation(100000L * (1 + mongoTemplate.count(null, OrganisationConfig.class, collectionName)));
            mongoTemplate.save(organisationConfig, collectionName);
            organisationConfig = mongoTemplate.findAndModify(query, update, OrganisationConfig.class, collectionName);
        }
        return organisationConfig.getNextOrderNumberForOrganisation().toString();
    }

    public Long getHighestOrderNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_class").is(Claim.class.getName()));
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));

        List<Claim> claims = mongoTemplate.find(query, Claim.class);

        return claims.stream().map(Claim::getOrderNumber).mapToLong(Long::parseLong).max().orElse(100000L);
    }
}
