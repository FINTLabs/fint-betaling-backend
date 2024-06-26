package no.fint.betaling.factory

import no.fint.betaling.claim.ClaimFactory
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Specification

import java.time.LocalDate

class ClaimFactorySpec extends Specification {
    private ClaimFactory claimFactory
    private BetalingObjectFactory betalingObjectFactory

    void setup() {
        claimFactory = new ClaimFactory()
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Create claims from Order"() {
        given:
        def order = betalingObjectFactory.newOrder()

        when:
        def claims = claimFactory.createClaims(order)

        then:
        claims.get(0).createdDate.toLocalDate() == LocalDate.now()
        claims.get(0).lastModifiedDate.toLocalDate() == LocalDate.now()
        claims.get(0).originalAmountDue == 1000000
        claims.get(0).requestedNumberOfDaysToPaymentDeadline == '7'
        claims.get(0).customerName == 'Ola Testesen'
        claims.get(0).createdByEmployeeNumber == '2001'
        claims.get(0).organisationUnit.name == 'HVS'
        claims.get(0).principalCode == 'tt0093780'
        claims.get(0).orderItems.get(0).description == 'Monkeyballs'
        claims.get(0).claimStatus == ClaimStatus.STORED
    }
}
