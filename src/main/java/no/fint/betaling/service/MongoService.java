package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.Fakturagrunnlag;
import no.fint.betaling.model.Kunde;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Betaling saveFakturagrunnlag(String orgId, Fakturagrunnlag fakturagrunnlag, Kunde kunde) {
        Betaling betaling = new Betaling();
        betaling.setFakturagrunnlag(fakturagrunnlag);
        betaling.setKunde(kunde);
        betaling.setOrdrenummer("123test");
        mongoTemplate.save(betaling, orgId);
        return betaling;
    }

    public List<Betaling> getFakturagrunnlag(String orgId, Query query) {
        return mongoTemplate.find(query, Betaling.class, orgId);
    }
}
