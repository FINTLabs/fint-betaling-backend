package no.fint.betaling.service

import no.fint.betaling.claim.ClaimFactory

import no.fint.betaling.claim.ClaimDatabaseService
import no.fint.betaling.claim.InvoiceFactory
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.ClaimsDatePeriod
import no.fint.betaling.model.Order
import no.fint.betaling.claim.ClaimRepository
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import spock.lang.Specification

import java.time.LocalDateTime

class ClaimDatabaseServiceSpec extends Specification {
    ClaimDatabaseService claimDatabaseService
    RestUtil restUtil
    ClaimRepository claimRepository
    ClaimFactory claimFactory
    InvoiceFactory invoiceFactory
    FintClient fintClient
    BetalingObjectFactory betalingObjectFactory

    void setup() {
        restUtil = Mock()
        claimRepository = Mock()
        claimFactory = Mock()
        invoiceFactory = Mock()
        fintClient = Mock()

        claimDatabaseService = new ClaimDatabaseService(claimRepository, claimFactory)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Given valid order, create and store claims"() {
        given:
        def order = betalingObjectFactory.newOrder()
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)

        when:
        def claims = claimDatabaseService.storeClaims(order)

        then:
        1 * claimFactory.createClaims(_ as Order) >> [claim]
        1 * claimRepository.storeClaim(_ as Claim) >> claim
        claims.size() == 1
        claims.every { it.orderItems.size() == 1 }
        claims.every { it.orderNumber == 12345L }
        claims.every { it.customerName == 'Ola Testesen' }
    }

    def "Get claims by status returns list of claims"() {
        given:
        def claimStatuses = [ClaimStatus.SENT, ClaimStatus.ACCEPTED]
        def expectedClaims = [
                new Claim(orgId: 123, orderNumber: 12345, claimStatus: ClaimStatus.SENT),
                new Claim(orgId: 456, orderNumber: 67890, claimStatus: ClaimStatus.SENT)
        ]
        claimRepository.get(claimStatuses as ClaimStatus[]) >> expectedClaims

        when:
        def actualClaims = claimDatabaseService.getClaimsByStatus(claimStatuses as ClaimStatus[])

        then:
        actualClaims == expectedClaims
    }

    def "Get all claims by status returns list"() {
        given:
        def period = ClaimsDatePeriod.ALL
        String organisationNumber = "12345"
        ClaimStatus[] statuses = [ClaimStatus.STORED, ClaimStatus.SENT]
        LocalDateTime date = LocalDateTime.now()
        def expectedClaims = [
                betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED),
                betalingObjectFactory.newClaim(12346L, ClaimStatus.STORED)
        ]

        claimRepository.getByDateAndSchoolAndStatus(_, _, _) >> expectedClaims

        when:
        def actualClaims = claimDatabaseService.getClaimsByPeriodAndOrganisationnumberAndStatus(period, organisationNumber, statuses)

        then:
        actualClaims.size() == expectedClaims.size()
    }

    def "Get claims by customer name returns list of claims matching name"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)


        when:
        def claims = claimDatabaseService.getClaimsByCustomerName('Ola Testesen')

        then:
        1 * claimRepository.getByCustomerName('Ola Testesen') >> [claim]
        claims.size() == 1
        claims.get(0).customerName == 'Ola Testesen'
    }

    def "Get count of claims by status"() {

        when:
        def claims = claimDatabaseService.countClaimsByStatus([ClaimStatus.ERROR] as ClaimStatus[], '')

        then:
        1 * claimRepository.countByStatus(ClaimStatus.ERROR) >> 8
        claims == 8
    }


    def "Get count of claims by status and days"() {

        when:
        def claims = claimDatabaseService.countClaimsByStatus([ClaimStatus.SENT] as ClaimStatus[], '14')

        then:
        1 * claimRepository.countByStatusAndDays(14, ClaimStatus.SENT) >> 143
        claims == 143
    }

    def "Get claims given valid order number returns list of claims matching order number"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)

        when:
        def result = claimDatabaseService.getClaimByOrderNumber(12L)

        then:
        1 * claimRepository.get(12L) >> claim
        result.orderNumber == 12345L
    }
}
