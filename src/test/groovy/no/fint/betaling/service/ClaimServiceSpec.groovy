package no.fint.betaling.service

import no.fint.betaling.claim.ClaimFactory
import no.fint.betaling.claim.ClaimFetcherService
import no.fint.betaling.claim.ClaimService
import no.fint.betaling.claim.InvoiceFactory
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.ClaimsDatePeriod
import no.fint.betaling.model.Order
import no.fint.betaling.claim.ClaimRepository
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDateTime

class ClaimServiceSpec extends Specification {
    ClaimService claimService
    RestUtil restUtil
    ClaimRepository claimRepository
    ClaimFactory claimFactory
    InvoiceFactory invoiceFactory
    FintClient fintClient
    BetalingObjectFactory betalingObjectFactory
    ClaimFetcherService claimFetcherService

    void setup() {
        restUtil = Mock()
        claimRepository = Mock()
        claimFactory = Mock()
        invoiceFactory = Mock()
        fintClient = Mock()
        claimFetcherService = Mock()

        claimService = new ClaimService(restUtil, claimRepository, claimFactory, invoiceFactory, fintClient, claimFetcherService)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Given valid order, create and store claims"() {
        given:
        def order = betalingObjectFactory.newOrder()
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)

        when:
        def claims = claimService.storeClaims(order)

        then:
        1 * claimFactory.createClaims(_ as Order) >> [claim]
        1 * claimRepository.storeClaim(_ as Claim) >> claim
        claims.size() == 1
        claims.every { it.orderItems.size() == 1 }
        claims.every { it.orderNumber == 12345L }
        claims.every { it.customerName == 'Ola Testesen' }
    }

    def "Given valid claims, send invoices and update claims"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)
        def header = new HttpHeaders();
        header.setLocation(new URI('link.to.Location'))

        claimFetcherService.getUnsentClaims() >> [claim]
        restUtil.post(*_) >> Mono.just(header)

        when:
        def claims = claimService.sendClaims([12345L])

        then:
        StepVerifier
                .create(claims)
                .assertNext({ c ->
                    assert c.claimStatus == ClaimStatus.SENT
                    assert c.invoiceUri == 'link.to.Location'
                })
                .expectComplete()
                .verify()
    }

    def "Send claim as inovice returns status"() {
//        given:
//        def invoice = betalingObjectFactory.newFakturagrunnlag()
//        def thisIsTheInvoice = betalingObjectFactory.newFaktura()
//
//        when:
//        def response = claimService.updateClaim(invoice)
//
//        then:
//
//
//
//        1* claimRepository.get(12345L)  >> betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)
//        1 * restUtil.post(*_) >> Mono.just(new URI('link.to.Location'))
//        1 * fintClient.getFaktura(*_) >> [thisIsTheInvoice]
//        1 * fintClient.setInvoiceUri(_) >> Optional.of('link.to.Location')
//        response.claimStatus == ClaimStatus.ACCEPTED.toString()
    }

    @Ignore
    def "Update claims fetches invoices and updates claims"() {
//        given:
//        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)
//        def invoice = betalingObjectFactory.newFakturagrunnlag()
//
//        when:
//        claimService.updateClaims()
//
//        then:
//        1 * claimRepository.getAll(_ as Query) >> [claim]
//        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> invoice
//        1 * claimRepository.updateClaim(_ as Query, _ as Update)
    }

    def "Get claims by status returns list of claims"() {
        given:
        def claimStatuses = [ClaimStatus.SENT, ClaimStatus.ACCEPTED]
        def expectedClaims = [
                new Claim(orgId: 123, orderNumber: 12345, claimStatus: ClaimStatus.SENT),
                new Claim(orgId: 456, orderNumber: 67890, claimStatus: ClaimStatus.SENT)
        ]
        claimFetcherService.getClaimsByStatus(claimStatuses as ClaimStatus[]) >> expectedClaims

        when:
        def actualClaims = claimFetcherService.getClaimsByStatus(claimStatuses as ClaimStatus[])

        then:
        actualClaims == expectedClaims
    }

//    @Ignore("Must be rewritten from mongoDb to postgresql")
//    def "Update claim given valid invoice updates claim"() {
//        given:
//        def invoice = betalingObjectFactory.newFakturagrunnlag()
//
//        when:
//        claimService.updateClaim(invoice)
//
//        then:
//        1 * claimRepository.updateClaim(_ as Query, _ as Update)
//    }

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

        claimFetcherService.getClaimsByPeriodAndOrganisationnumberAndStatus(_, _, _) >> expectedClaims

        when:
        def actualClaims = claimFetcherService.getClaimsByPeriodAndOrganisationnumberAndStatus(period, organisationNumber, statuses)

        then:
        actualClaims.size() == expectedClaims.size()
    }

    def "Get claims by customer name returns list of claims matching name"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)


        when:
        def claims = claimFetcherService.getClaimsByCustomerName('Ola Testesen')

        then:
        1 * claimFetcherService.getClaimsByCustomerName('Ola Testesen') >> [claim]
        claims.size() == 1
        claims.get(0).customerName == 'Ola Testesen'
    }

//    @Ignore("Must be rewritten from mongoDb to postgresql")
//    def "Get claims given valid order number returns list of claims matching order number"() {
//        given:
//        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)
//
//        when:
//        def claims = claimService.getClaimsByOrderNumber('12')
//
//        then:
//        1 * claimRepository.getAll(_ as Query) >> [claim]
//        claims.size() == 1
//        claims.get(0).orderNumber == '12345'
//    }

//    @Ignore("Must be rewritten from mongoDb to postgresql")
//    def 'Fetch links to Faktura for Fakturagrunnlag'() {
//        given:
//        def mapper = new ObjectMapper()
//        def fakturagrunnlag = mapper.readValue(getClass().getResourceAsStream('/dummy_fakturagrunnlag.json'), FakturagrunnlagResource)
//
//        when:
//        claimService.updateClaim(fakturagrunnlag)
//
//        then:
//        2 * restUtil.get(*_) >>> [Mono.just(betalingObjectFactory.newFaktura()), Mono.just(betalingObjectFactory.newFaktura())]
//        1 * claimRepository.updateClaim(_ as Query,
//                _/*{it.modifierOps['$set'].every { it.key ['invoiceUri', 'invoiceNumbers', 'invoiceDate', 'paymentDueDate', 'amountDue'] }}*/
//        )
//    }

    def "Get count of claims by status"() {

        when:
        def claims = claimService.countClaimsByStatus([ClaimStatus.ERROR] as ClaimStatus[], '')

        then:
        1 * claimRepository.countByStatus(ClaimStatus.ERROR) >> 8
        claims == 8
    }


    def "Get count of claims by status and days"() {

        when:
        def claims = claimService.countClaimsByStatus([ClaimStatus.SENT] as ClaimStatus[], '14')

        then:
        1 * claimRepository.countByStatusAndDays(14, ClaimStatus.SENT) >> 143
        claims == 143
    }
}
