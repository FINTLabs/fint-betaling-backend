package no.fint.betaling.service;

import no.fint.betaling.model.OrgConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdernumberService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getOrdernumber(String orgId) {
        String id = orgId.replace(".", "");
        return String.format("%s%s", id, getAndUpdateLastOrdernumber(orgId));
    }

    private String getAndUpdateLastOrdernumber(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgId").is(orgId));
        query.addCriteria(Criteria.where("sisteOrdrenummer").exists(true));

        OrgConfig orgConfig = mongoTemplate.findOne(query, OrgConfig.class, orgId);
        Long ordernumber = null;

        if (orgConfig == null) {
            orgConfig = new OrgConfig();
            orgConfig.setOrgId(orgId);
            orgConfig.setSisteOrdrenummer(0L);
            ordernumber = 0L;
            mongoTemplate.save(orgConfig, orgId);
        } else {
            ordernumber = orgConfig.getSisteOrdrenummer() + 1;
            Update update = new Update();
            update.set("sisteOrdrenummer", ordernumber);

            mongoTemplate.updateFirst(query, update, orgId);
        }

        return String.format("%s", ordernumber);
    }

    public String getOrdernumberFromNumber(String orgId, String number){
        return String.format("%s%s", orgId.replace(".",""), number);
    }
}
