package no.fint.betaling.repository

import no.fint.betaling.model.Claim
import no.fint.betaling.model.Customer
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import spock.lang.Specification

class ClaimRepositorySpec extends Specification {

    private String orgId
    private MongoTemplate mongoTemplate
    private ClaimRepository claimRepository

    void setup() {
        orgId = 'test.no'
        mongoTemplate = Mock(MongoTemplate)
        claimRepository = new ClaimRepository(mongoTemplate: mongoTemplate)
    }

    def "Set payment given valid data sends Betaling and orgId to mongotemplate"() {
        given:
        def customer = new Customer(name: 'Testesen')

        when:
        claimRepository.setClaim(new Claim(invoiceUri: 'link.to.FakturagrunnlagResource'.toURI(), customer: customer))

        then:
        1 * mongoTemplate.save(_ as Claim)
    }

    def "Get payment returns list"() {
        given:
        def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        when:
        def claims = claimRepository.getClaims(query)

        then:
        1 * mongoTemplate.find(_ as Query, _ as Class<Claim>) >> [new Claim()]
        claims.size() == 1
    }

    def "Update payment given valid org id, valid update and valid query"() {
        given:
        def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        def update = new Update()
        update.set('someValue','testValue')

        when:
        claimRepository.updateClaim(query, update)

        then:
        1 * mongoTemplate.upsert(_ as Query, _ as Update, _ as Class<Claim>)
    }
}
