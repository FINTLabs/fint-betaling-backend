package no.fint.betaling.repository

import no.fint.betaling.config.Endpoints
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.util.CloseableIterator
import spock.lang.Specification

import java.util.stream.Stream

class ClaimRepositorySpec extends Specification {

    private Endpoints endpoints
    private MongoTemplate mongoTemplate
    private ClaimRepository claimRepository
    private BetalingObjectFactory betalingObjectFactory;

    void setup() {
        endpoints = Mock(Endpoints)
        mongoTemplate = Mock(MongoTemplate)
        claimRepository = new ClaimRepository(endpoints, mongoTemplate)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Set payment given valid data sends Betaling and orgId to mongotemplate"() {
        given:
        def claim = betalingObjectFactory.newClaim('123', ClaimStatus.STORED)

        when:
        claimRepository.storeClaim(claim)

        then:
        1 * mongoTemplate.save(_ as Claim)
        claim.orderNumber == '100001'
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

    def "Get highest order number"() {
        given:
        def lowClaim = betalingObjectFactory.newClaim('1234', ClaimStatus.SENT)
        def highClaim = betalingObjectFactory.newClaim('5678', ClaimStatus.STORED)

        when:
        claimRepository.setHighestOrderNumber()

        then:
        1 * mongoTemplate.stream(_ as Query, _ as Class<Claim>) >> [highClaim, lowClaim].stream()
        claimRepository.orderNumberCounter.get() == 5678
    }
}
