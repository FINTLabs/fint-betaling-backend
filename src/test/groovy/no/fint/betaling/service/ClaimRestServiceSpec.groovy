package no.fint.betaling.service

import no.fint.betaling.claim.*
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

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

    def "Send claim as invoice returns status"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)
        def invoice = betalingObjectFactory.newFakturagrunnlag()
        def headers = new HttpHeaders()
        headers.setLocation(new URI('link.to.Location'))

        when:
        def response = claimService.sendClaim(claim).block()

        then:
        1 * invoiceFactory.createInvoice(claim) >> invoice
        1 * restUtil.post(*_) >> Mono.just(headers)
        response.claimStatus == ClaimStatus.SENT
        claim.invoiceUri == 'link.to.Location'
    }

    def "Update claims fetches invoices and updates claims"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)
        claim.setInvoiceUri('link.to.Location')
        def invoice = betalingObjectFactory.newFakturagrunnlag()

        when:
        def result = claimService.updateClaimStatus(claim).block()

        then:
        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, 'link.to.Location') >> Mono.just(invoice)
        1 * claimRepository.get(_) >> claim
        1 * fintClient.getSelfLink(invoice) >> Optional.empty()
        1 * fintClient.getFaktura(_) >> Mono.just([])
        result == null
    }

    def "Update claims should return Mono.empty() when invoiceUri is empty"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)
        claim.setInvoiceUri('') // Set the invoiceUri to an empty string or null

        when:
        def result = claimService.updateClaimStatus(claim)

        then:
        0 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) // Ensure restUtil.get is never called
        result.blockOptional().isEmpty() // Check that the result is Mono.empty()
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
