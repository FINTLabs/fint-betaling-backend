package no.fint.betaling.service

import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Kunde
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
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

    def "Set payment given valid data sends Betaling and orgId to mongotemplate"() {
        given:
        def fakturagrunnlag = new FakturagrunnlagResource(total: 1000)
        def kunde = new Kunde(navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))

        when:
        mongoService.setPayment(orgId, new Betaling(fakturagrunnlag: fakturagrunnlag, kunde: kunde))

        then:
        1 * mongoTemplate.save(_ as Betaling, 'test.no')
    }

    def "Get payment returns list"() {
        given:
        def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        when:
        def listBetaling = mongoService.getPayments(orgId, query)

        then:
        1 * mongoTemplate.find(_ as Query, Betaling.class, 'test.no') >> [new Betaling()]
        listBetaling.size() == 1
    }

    def "Update payment given valid org id, valid update and valid query"() {
        given:
        def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        def update = new Update()
        update.set('someValue','testValue')

        when:
        mongoService.updatePayment('test.no', query, update)

        then:
        1 * mongoTemplate.upsert(_ as Query, _ as Update, _ as String)
    }
}
