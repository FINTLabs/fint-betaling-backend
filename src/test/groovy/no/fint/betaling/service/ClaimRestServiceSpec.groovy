package no.fint.betaling.service

import no.fint.betaling.claim.ClaimDatabaseService
import no.fint.betaling.claim.ClaimFactory
import no.fint.betaling.claim.ClaimRestService
import no.fint.betaling.claim.ClaimRestStatusService
import no.fint.betaling.claim.InvoiceFactory
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.ClaimsDatePeriod
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

class ClaimRestServiceSpec extends Specification {
    RestUtil restUtil
    ClaimRepository claimRepository
    ClaimFactory claimFactory
    InvoiceFactory invoiceFactory
    FintClient fintClient
    BetalingObjectFactory betalingObjectFactory
    ClaimDatabaseService claimDatabaseService
    ClaimRestService claimService
    ClaimRestStatusService claimRestStatusService

    void setup() {
        restUtil = Mock()
        claimRepository = Mock()
        claimFactory = Mock()
        invoiceFactory = Mock()
        fintClient = Mock()
        claimDatabaseService = Mock()
        claimRestStatusService = Mock()

        claimService = new ClaimRestService(restUtil, fintClient, invoiceFactory, claimRepository, claimDatabaseService, claimRestStatusService)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Given valid claims, send invoices and update claims"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)
        def header = new HttpHeaders();
        header.setLocation(new URI('link.to.Location'))

        claimDatabaseService.getUnsentClaims() >> [claim]
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

}
