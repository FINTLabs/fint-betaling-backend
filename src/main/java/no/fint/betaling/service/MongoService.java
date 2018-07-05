package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import no.fint.model.administrasjon.okonomi.Varelinje;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void setPayment(String orgId, Betaling payment) {
        mongoTemplate.save(payment, orgId);
    }

    public List<Betaling> getPayments(String orgId, Query query) {
        return mongoTemplate.find(query, Betaling.class, orgId);
    }

    public void updatePayment(String orgId, Query query, Update update) {
        mongoTemplate.upsert(query, update, orgId);
    }

    public void setOrderLine(String orgId, VarelinjeResource varelinje) {
        mongoTemplate.save(varelinje, orgId);
    }

    public List<Varelinje> getOrderLine(String orgId, Query query) {
        return mongoTemplate.find(query, Varelinje.class, orgId);
    }


}
