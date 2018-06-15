package no.fint.betaling.service;

import no.fint.betaling.model.Fakturagrunnlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveFakturagrunnlag(Fakturagrunnlag fakturagrunnlag){
        mongoTemplate.save(fakturagrunnlag);
    }
}
