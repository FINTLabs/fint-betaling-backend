package no.fint.betaling.service;

import no.fint.betaling.model.Varelinje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderLineService {

    @Autowired
    private MongoService mongoService;

    public List<Varelinje> getOrderLines(String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mvakode").exists(true));
        query.addCriteria(Criteria.where("oppdragsgiver").exists(true));
        query.addCriteria(Criteria.where("pris").exists(true));

        return mongoService.getOrderLine(orgId, query);
    }

    public Varelinje setOrderLine(String orgId, Varelinje orderLine) {
        mongoService.setOrderLine(orgId, orderLine);
        return orderLine;
    }
}
