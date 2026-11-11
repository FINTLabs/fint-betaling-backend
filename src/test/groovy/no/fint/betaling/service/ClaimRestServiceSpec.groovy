package no.fint.betaling.service

import no.fint.betaling.claim.*
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.ClaimsDatePeriod
import no.fint.betaling.util.BetalingObjectFactory
import no.novari.fint.model.resource.okonomi.faktura.FakturagrunnlagResource
import org.springframework.http.HttpHeaders
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClientResponseException
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
    ClaimRestService claimRestService
    ClaimRestStatusService claimRestStatusService

    void setup() {
        restUtil = Mock()
        claimRepository = Mock()
        claimFactory = Mock()
        invoiceFactory = Mock()
        fintClient = Mock()
        claimDatabaseService = Mock()
        claimRestStatusService = Mock()

        claimRestService = new ClaimRestService(restUtil, fintClient, invoiceFactory, claimRepository, claimDatabaseService, claimRestStatusService)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Given valid claims, send invoices and update claims"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)
        def header = new HttpHeaders()

        header.setLocation(URI.create(
                'https://beta.felleskomponent.no/okonomi/faktura/fakturagrunnlag/status/12345'
        ))

        claimDatabaseService.getUnsentClaims() >> [claim]
        restUtil.post(*_) >> Mono.just(header)

        when:
        def claims = claimRestService.sendClaims([12345L])

        then:
        StepVerifier
                .create(claims)
                .assertNext { c ->
                    assert c.claimStatus == ClaimStatus.SENT
                    assert c.invoiceUri == 'https://beta.felleskomponent.no/okonomi/faktura/fakturagrunnlag/status/12345'
                }
                .expectComplete()
                .verify()
    }

    def "Send claim preserves POST Location for status polling"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.STORED)
        def invoice = betalingObjectFactory.newFakturagrunnlag()
        def headers = new HttpHeaders()
        headers.setLocation(URI.create('https://link.to.Location'))

        when:
        def response = claimRestService.sendClaim(claim).block()

        then:
        1 * invoiceFactory.createInvoice(claim) >> invoice
        1 * restUtil.post(*_) >> Mono.just(headers)
        1 * claimRestStatusService.processRequest({
            it.is(claim) && it.invoiceUri == 'https://link.to.Location'
        })

        response.claimStatus == ClaimStatus.SENT
        claim.invoiceUri == 'https://link.to.Location'
    }

    def "Update claims uses filter URL while preserving stored location #storedLocation"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)
        claim.setInvoiceUri(storedLocation)
        ReflectionTestUtils.setField(claimRestService, 'invoiceEndpoint', invoiceEndpoint)
        def invoice = betalingObjectFactory.newFakturagrunnlag()

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, expectedFilterUrl) >> Mono.just(invoice)
        1 * claimRepository.get(_) >> claim
        1 * fintClient.getSelfLink(invoice) >> Optional.empty()
        1 * fintClient.getFaktura(_) >> Mono.just([])
        1 * fintClient.getFakturanummere([]) >> ([] as Set)
        1 * fintClient.getInvoiceDate([]) >> Optional.empty()
        1 * fintClient.getPaymentDueDate([]) >> Optional.empty()
        1 * fintClient.getAmountDue([]) >> Optional.empty()
        1 * claimRepository.save(claim)
        result.is(claim)
        claim.claimStatus == ClaimStatus.ACCEPTED
        claim.invoiceUri == storedLocation

        where:
        storedLocation                                                                   | invoiceEndpoint                                                   | expectedFilterUrl
        'https://api.example.com/okonomi/faktura/fakturagrunnlag/status/12345'              | '/okonomi/faktura/fakturagrunnlag'                                  | "/okonomi/faktura/faktura?\$filter=fakturanummer eq '12345'"
        'https://api.example.com/okonomi/faktura/fakturagrunnlag/systemid/abc'              | '/okonomi/faktura/fakturagrunnlag'                                  | "/okonomi/faktura/faktura?\$filter=fakturanummer eq '12345'"
        "https://api.example.com/okonomi/faktura/faktura?\$filter=fakturanummer eq '12345'"  | '/okonomi/faktura/fakturagrunnlag'                                  | "/okonomi/faktura/faktura?\$filter=fakturanummer eq '12345'"
        'https://api.example.com/okonomi/faktura/fakturagrunnlag/systemid/abc'              | 'https://api.example.com/okonomi/faktura/fakturagrunnlag/'            | "https://api.example.com/okonomi/faktura/faktura?\$filter=fakturanummer eq '12345'"
    }

    def "Update failure persists UPDATE_ERROR for #failure"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = 'https://api.example.com/okonomi/faktura/fakturagrunnlag/systemid/abc'
        ReflectionTestUtils.setField(claimRestService, 'invoiceEndpoint', '/okonomi/faktura/fakturagrunnlag')
        restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> Mono.error(failure)

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        result == null
        1 * claimRepository.save({
            it.is(claim) && it.claimStatus == ClaimStatus.UPDATE_ERROR && it.statusMessage == expectedMessage
        })

        where:
        failure                                                                               | expectedMessage
        new NullPointerException()                                                            | 'NullPointerException'
        new IllegalStateException('Invalid invoice response')                                  | 'Invalid invoice response'
        WebClientResponseException.create(500, 'Server Error', HttpHeaders.EMPTY, new byte[0], null) | '500 Server Error'
    }

    def "Invalid invoice response persists UPDATE_ERROR"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = 'https://api.example.com/okonomi/faktura/fakturagrunnlag/systemid/abc'
        ReflectionTestUtils.setField(claimRestService, 'invoiceEndpoint', '/okonomi/faktura/fakturagrunnlag')
        restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> Mono.just(new FakturagrunnlagResource())

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        result == null
        1 * claimRepository.save({
            it.is(claim) && it.claimStatus == ClaimStatus.UPDATE_ERROR && it.statusMessage
        })
    }

    def "Update claims should return Mono.empty() when invoiceUri is empty"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)
        claim.setInvoiceUri('') // Set the invoiceUri to an empty string or null

        when:
        def result = claimRestService.updateClaimStatus(claim)

        then:
        0 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) // Ensure restUtil.get is never called
        result.blockOptional().isEmpty() // Check that the result is Mono.empty()
    }
}
