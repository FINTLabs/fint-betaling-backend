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
        query.addCriteria(Criteria.where("sisteOrdrenummer").is(""));

        List<OrgConfig> orgConfig = mongoTemplate.find(query, OrgConfig.class, orgId);
        Long ordernumber = null;

        if (orgConfig.size() == 0) {
            OrgConfig orgConfigNew = new OrgConfig();
            orgConfigNew.setOrgId(orgId);
            orgConfigNew.setSisteOrdrenummer(0L);

            ordernumber = 0L;
            mongoTemplate.save(orgConfigNew, orgId);
        } else {
            ordernumber = orgConfig.get(0).getSisteOrdrenummer() + 1;
            Update update = new Update();
            update.set("sisteOrdrenummer", ordernumber);

            mongoTemplate.updateFirst(query, update, orgId);
        }

        return String.format("%s", ordernumber);
    }
}
