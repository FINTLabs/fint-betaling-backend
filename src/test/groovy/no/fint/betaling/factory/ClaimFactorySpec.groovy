package no.fint.betaling.factory

import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.repository.ClaimRepository
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Specification

import java.time.LocalDate
import java.time.ZoneId

class ClaimFactorySpec extends Specification {
    private ClaimRepository claimRepository
    private ClaimFactory claimFactory
    private BetalingObjectFactory betalingObjectFactory

    void setup() {
        claimRepository = Mock()
        claimFactory = new ClaimFactory(claimRepository: claimRepository)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Create claims from Order"() {
        given:
        def order = betalingObjectFactory.newOrder()

        when:
        def claims = claimFactory.createClaim(order)

        then:
        1 * claimRepository.getHighestOrderNumber() >> 123
        claims.get(0).orderNumber == '124'
        claims.get(0).createdDate == LocalDate.now(ZoneId.systemDefault())
        claims.get(0).lastModifiedDate == LocalDate.now(ZoneId.systemDefault())
        claims.get(0).originalAmountDue == 1000000
        claims.get(0).requestedNumberOfDaysToPaymentDeadline == '7'
        claims.get(0).customer.name == 'Ola Testesen'
        claims.get(0).createdBy.name == 'Frank Testesen'
        claims.get(0).organisationUnit.name == 'HVS'
        claims.get(0).principal.code == 'tt0093780'
        claims.get(0).orderItems.get(0).description == 'Monkeyballs'
        claims.get(0).claimStatus == ClaimStatus.STORED
    }
}
