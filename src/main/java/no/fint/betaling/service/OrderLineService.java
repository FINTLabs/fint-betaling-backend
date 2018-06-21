package no.fint.betaling.service;

import no.fint.model.administrasjon.okonomi.Varelinje;
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
        query.addCriteria(Criteria.where("_class").is("no.fint.model.administrasjon.okonomi.Varelinje"));

        return mongoService.getOrderLine(orgId, query);
    }

    public Varelinje setOrderLine(String orgId, Varelinje orderLine) {
        mongoService.setOrderLine(orgId, orderLine);
        return orderLine;
    }
}
