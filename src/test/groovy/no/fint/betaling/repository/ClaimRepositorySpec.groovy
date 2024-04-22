package no.fint.betaling.repository

import no.fint.betaling.claim.ClaimJpaRepository
import no.fint.betaling.claim.ClaimRepository
import no.fint.betaling.claim.OrderItemJpaRepository
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.organisation.OrganisationJpaRepository
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Ignore
import spock.lang.Specification

class ClaimRepositorySpec extends Specification {

    ClaimRepository claimRepository
    BetalingObjectFactory betalingObjectFactory
    ClaimJpaRepository claimJpaRepository
    OrganisationJpaRepository organisationJpaRepository
    OrderItemJpaRepository orderItemJpaRepository

    void setup() {
        claimJpaRepository = Mock()
        organisationJpaRepository = Mock()
        orderItemJpaRepository = Mock()
        claimRepository = new ClaimRepository(claimJpaRepository, organisationJpaRepository, orderItemJpaRepository)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Set payment given valid data sends Betaling and orgId to claimJpaRepository"() {
        given:
        def claim = betalingObjectFactory.newClaim(123L, ClaimStatus.STORED)

        when:
        claimRepository.storeClaim(claim)

        then:
        1 * claimJpaRepository.save(claim) >> claim
    }

    @Ignore("Changed into using sequence")
    def "Get highest order number"() {
        when:
        claimRepository.setHighestOrderNumber()

        then:
        1 * claimJpaRepository.findHighestOrderNumber() >> Optional.of(5678L);
        claimRepository.orderNumberCounter.get() == 5678
    }

    @Ignore("Changed into using sequence")
    def "Get highest order number - use default value"(){
            when:
            claimRepository.setHighestOrderNumber()

            then:
            1 * claimJpaRepository.findHighestOrderNumber() >> Optional.empty();
            claimRepository.orderNumberCounter.get() == 10000L

    }
}
