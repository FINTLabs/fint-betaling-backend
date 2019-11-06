package no.fint.betaling.repository;

import no.fint.betaling.config.OrganisationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class OrderNumberRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getOrderNumber(String orgId) {
        return getAndUpdateLastOrderNumber(orgId);
    }

    private String getAndUpdateLastOrderNumber(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgId").is(orgId));
        query.addCriteria(Criteria.where("_class").is(OrganisationConfig.class.getName()));

        Update update = new Update();
        update.inc("nextOrderNumberForOrganisation  ", 1);

        FindAndModifyOptions.options().returnNew(false);
        OrganisationConfig organisationConfig =
                mongoTemplate.findAndModify(query, update, OrganisationConfig.class, orgId);

        if (organisationConfig == null) {
            organisationConfig = new OrganisationConfig();
            organisationConfig.setOrgId(orgId);
            organisationConfig.setNextOrderNumberForOrganisation(100000L * (1 + mongoTemplate.count(null, OrganisationConfig.class)));
            mongoTemplate.save(organisationConfig, orgId);
            organisationConfig = mongoTemplate.findAndModify(query, update, OrganisationConfig.class, orgId);
        }
        return organisationConfig.getNextOrderNumberForOrganisation().toString();
    }
}
