package no.fint.betaling.service

import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Fakturagrunnlag
import no.fint.betaling.model.Kunde
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

    def "Save fakturagrunnlag given valid data returns Betaling"() {
        given:
        def fakturagrunnlag = new Fakturagrunnlag(systemId: new Identifikator(identifikatorverdi: 'test'), total: 1000)
        def kunde = new Kunde(navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))

        when:
        def betaling = mongoService.saveFakturagrunnlag(orgId, fakturagrunnlag, kunde)

        then:
        1 * mongoTemplate.save(_, 'test.no')
        betaling.fakturagrunnlag.total == 1000
        betaling.kunde.navn.etternavn == 'Testesen'
    }

    def "Get fakturagrunnlag returns list"() {
        given:
        def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        when:
        def listBetaling = mongoService.getFakturagrunnlag(orgId, query)

        then:
        1 * mongoTemplate.find(_, Betaling.class, 'test.no') >> [new Betaling()]
        listBetaling.size() == 1
    }
}
