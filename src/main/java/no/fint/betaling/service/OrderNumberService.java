package no.fint.betaling.service;

import no.fint.betaling.model.OrgConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class OrderNumberService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getOrderNumber(String orgId) {
        String id = orgId.replace(".", "");
        return String.format("%s%s", id, getAndUpdateLastOrderNumber(orgId));
    }

    public String getOrderNumberFromNumber(String orgId, String number) {
        return String.format("%s%s", orgId.replace(".", ""), number);
    }

    private String getAndUpdateLastOrderNumber(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgId").is(orgId));
        query.addCriteria(Criteria.where("_class").is("no.fint.betaling.model.OrgConfig"));

        Update update = new Update();
        update.inc("nesteOrdrenummer", 1);

        FindAndModifyOptions.options().returnNew(false);
        OrgConfig orgConfig = mongoTemplate.findAndModify(query, update, OrgConfig.class, orgId);

        if (orgConfig == null) {
            orgConfig = new OrgConfig();
            orgConfig.setOrgId(orgId);
            mongoTemplate.save(orgConfig, orgId);
            orgConfig = mongoTemplate.findAndModify(query, update, OrgConfig.class, orgId);
        }
        return String.format("%s", orgConfig.getNesteOrdrenummer());
    }
}
