package no.fint.betaling.service

import no.fint.betaling.model.Betaling

import no.fint.betaling.model.Kunde
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

class MongoServiceSpec extends Specification {

    private String orgId
    private MongoTemplate mongoTemplate
    private MongoService mongoService

    void setup() {
        orgId = 'test.no'
        mongoTemplate = Mock(MongoTemplate)
        mongoService = new MongoService(mongoTemplate: mongoTemplate)
    }

    def "Save fakturagrunnlag given valid data sends Betaling and orgId to mongotemplate"() {
        given:
        def fakturagrunnlag = new Fakturagrunnlag(total: 1000)
        def kunde = new Kunde(navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))

        when:
        mongoService.setPayment(orgId, new Betaling(fakturagrunnlag: fakturagrunnlag, kunde: kunde))

        then:
        1 * mongoTemplate.save(_, 'test.no')
    }

    def "Get fakturagrunnlag returns list"() {
        given:
        def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        when:
        def listBetaling = mongoService.getPayments(orgId, query)

        then:
        1 * mongoTemplate.find(_, Betaling.class, 'test.no') >> [new Betaling()]
        listBetaling.size() == 1
    }
}
