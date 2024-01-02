package no.fint.betaling.repository

import no.fint.betaling.config.Endpoints
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Ignore
import spock.lang.Specification

class ClaimRepositorySpec extends Specification {

    ClaimRepository claimRepository
    BetalingObjectFactory betalingObjectFactory
    ClaimJpaRepository claimJpaRepository


    void setup() {
        claimJpaRepository = Mock()
        claimRepository = new ClaimRepository(claimJpaRepository)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Set payment given valid data sends Betaling and orgId to claimJpaRepository"() {
        given:
        def claim = betalingObjectFactory.newClaim(123L, ClaimStatus.STORED)

        when:
        claimRepository.storeClaim(claim)

        then:
        1 * claimJpaRepository.save(claim)
        claim.orderNumber == 100001L
    }

    @Ignore("Must be rewritten from mongoDb to postgresql")
    def "Get payment returns list"() {
        given:
        //def query = new Query()
        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))

        when:
        def claims = claimRepository.getAll(query)

        then:
        //1 * mongoTemplate.find(_ as Query, _ as Class<Claim>) >> [new Claim()]
        claims.size() == 1
    }

    @Ignore("Must be rewritten from mongoDb to postgresql")
    def "Update payment given valid org id, valid update and valid query"() {
//        given:
//        def query = new Query()
//        query.addCriteria(Criteria.where('someValue').is('someOtherValue'))
//
//        def update = new Update()
//        update.set('someValue','testValue')
//
//        when:
//        claimRepository.updateClaim(query, update)
//
//        then:
//        1 * mongoTemplate.upsert(_ as Query, _ as Update, _ as Class<Claim>)
    }

    @Ignore("Must be rewritten from mongoDb to postgresql")
    def "Get highest order number"() {
        given:
        def lowClaim = betalingObjectFactory.newClaim(1234L, ClaimStatus.SENT)
        def highClaim = betalingObjectFactory.newClaim(5678L, ClaimStatus.STORED)

        when:
        claimRepository.setHighestOrderNumber()

        then:
        //1 * mongoTemplate.stream(_ as Query, _ as Class<Claim>) >> [highClaim, lowClaim].stream()
        claimRepository.orderNumberCounter.get() == 5678
    }
}
