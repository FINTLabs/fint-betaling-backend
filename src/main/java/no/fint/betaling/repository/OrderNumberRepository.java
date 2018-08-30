package no.fint.betaling.repository;

import no.fint.betaling.model.OrgConfig;
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

    public long getOrderNumber(String orgId) {
        return getAndUpdateLastOrderNumber(orgId);
    }

    private long getAndUpdateLastOrderNumber(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgId").is(orgId));
        query.addCriteria(Criteria.where("_class").is(OrgConfig.class.getName()));

        Update update = new Update();
        update.inc("nesteOrdrenummer", 1);

        FindAndModifyOptions.options().returnNew(false);
        OrgConfig orgConfig = mongoTemplate.findAndModify(query, update, OrgConfig.class, orgId);

        if (orgConfig == null) {
            orgConfig = new OrgConfig();
            orgConfig.setOrgId(orgId);
            orgConfig.setNesteOrdrenummer(100000L * (1 + mongoTemplate.count(null, OrgConfig.class)));
            mongoTemplate.save(orgConfig, orgId);
            orgConfig = mongoTemplate.findAndModify(query, update, OrgConfig.class, orgId);
        }
        return orgConfig.getNesteOrdrenummer();
    }
}
