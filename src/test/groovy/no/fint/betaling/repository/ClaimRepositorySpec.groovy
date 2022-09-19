package no.fint.betaling.repository

import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.util.CloseableIterator
import spock.lang.Specification

class ClaimRepositorySpec extends Specification {

    private ReactiveMongoTemplate mongoTemplate
    private ClaimRepository claimRepository
    private BetalingObjectFactory betalingObjectFactory;

    void setup() {
        mongoTemplate = Mock(ReactiveMongoTemplate)
        claimRepository = new ClaimRepository(mongoTemplate: mongoTemplate)
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
        def claims = [highClaim, lowClaim].iterator()
        def iter = new CloseableIterator() {
            @Override
            void close() {

            }

            @Override
            boolean hasNext() {
                return claims.hasNext()
            }

            @Override
            Object next() {
                return claims.next()
            }
        }

        when:
        claimRepository.setHighestOrderNumber()

        then:
        1 * mongoTemplate.stream(_ as Query, _ as Class<Claim>) >> iter
        claimRepository.orderNumberCounter.get() == 5678
    }
}
