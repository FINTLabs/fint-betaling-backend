package no.fint.betaling.service

import no.fint.betaling.claim.*
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.ClaimsDatePeriod
import no.fint.betaling.util.BetalingObjectFactory
import no.novari.fint.model.resource.okonomi.faktura.FakturagrunnlagResource
import no.novari.fint.model.resource.okonomi.faktura.FakturaResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClient
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
        def invoice = new FakturaResource(betalt: false, kreditert: false)
        def headers = new HttpHeaders()
        headers.location = URI.create('https://api.example.com/okonomi/faktura/faktura/systemid/12345')

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        1 * restUtil.bodyless(expectedFilterUrl, { it.getFirst('Prefer') == 'respond-async' }) >> Mono.just(new ResponseEntity<Void>(headers, HttpStatus.FOUND))
        1 * restUtil.get(FakturaResource, headers.getLocation().toString()) >> Mono.just(invoice)
        0 * claimRepository.get(_)
        1 * fintClient.getFakturanummere([invoice]) >> (['12345'] as Set)
        1 * fintClient.getInvoiceDate([invoice]) >> Optional.empty()
        1 * fintClient.getPaymentDueDate([invoice]) >> Optional.empty()
        1 * fintClient.getAmountDue([invoice]) >> Optional.empty()
        1 * claimRepository.save(claim)
        result.is(claim)
        claim.claimStatus == ClaimStatus.ISSUED
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
        restUtil.bodyless(_, _) >> Mono.error(failure)

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
        def headers = new HttpHeaders()
        headers.location = URI.create('/invoice/12345')
        restUtil.bodyless(_, _) >> Mono.just(new ResponseEntity<Void>(headers, HttpStatus.FOUND))
        restUtil.get(FakturaResource, _) >> Mono.just(new FakturaResource(betalt: false, kreditert: false))
        fintClient.getFakturanummere(_) >> { throw new IllegalArgumentException('Missing invoice number') }

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        result == null
        1 * claimRepository.save({
            it.is(claim) && it.claimStatus == ClaimStatus.UPDATE_ERROR && it.statusMessage
        })
    }

    def "Filter Location is followed to update the claim to #expectedStatus without ordrenummer"() {
        given:
        def server = new MockWebServer()
        server.start()
        def baseUrl = server.url('/').toString()
        def realRestUtil = new RestUtil(WebClient.builder().baseUrl(baseUrl).build())
        realRestUtil.baseUrl = baseUrl
        def realFintClient = new FintClient(realRestUtil, null, null, null)
        def service = new ClaimRestService(realRestUtil, realFintClient, invoiceFactory,
                claimRepository, claimDatabaseService, claimRestStatusService)
        ReflectionTestUtils.setField(service, 'invoiceEndpoint', '/okonomi/faktura/fakturagrunnlag')
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = baseUrl + 'okonomi/faktura/fakturagrunnlag/systemid/abc'
        def location = claim.invoiceUri
        def invoicePath = '/okonomi/faktura/faktura/systemid/12345'
        server.enqueue(new MockResponse().setResponseCode(lookupStatus)
                .setHeader('Location', absoluteLocation ? baseUrl + invoicePath.substring(1) : invoicePath))
        server.enqueue(new MockResponse().setHeader('Content-Type', 'application/json').setBody("""
            {
                "fakturanummer":{"identifikatorverdi":"12345"},
                "betalt":${paid},"kreditert":${credited},"restbelop":250,
                "dato":"2026-11-01T00:00:00Z","forfallsdato":"2026-12-01T00:00:00Z"
            }
        """))

        when:
        def result = service.updateClaimStatus(claim).block()

        then:
        result.is(claim)
        claim.claimStatus == expectedStatus
        claim.invoiceNumbers == ['12345'] as Set
        claim.amountDue == 250L
        claim.invoiceDate != null
        claim.paymentDueDate != null
        claim.invoiceUri == location
        1 * claimRepository.save(claim)
        0 * claimRepository.get(_)
        def request = server.takeRequest()
        request.requestUrl.encodedPath() == '/okonomi/faktura/faktura'
        request.requestUrl.queryParameter('$filter') == "fakturanummer eq '12345'"
        request.getHeader('Prefer') == 'respond-async'
        server.takeRequest().path == invoicePath
        server.requestCount == 2

        cleanup:
        server.shutdown()

        where:
        paid  | credited | expectedStatus       | lookupStatus | absoluteLocation
        false | false    | ClaimStatus.ISSUED   | 302          | false
        true  | false    | ClaimStatus.PAID     | 202          | true
        false | true     | ClaimStatus.CREDITED | 200          | false
    }

    def "Missing lookup Location persists UPDATE_ERROR without fetching an invoice"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = 'https://api.example.com/okonomi/faktura/fakturagrunnlag/systemid/abc'
        ReflectionTestUtils.setField(claimRestService, 'invoiceEndpoint', '/okonomi/faktura/fakturagrunnlag')
        restUtil.bodyless(_, _) >> Mono.just(new ResponseEntity<Void>(HttpStatus.OK))

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        result == null
        0 * restUtil.get(_, _)
        1 * claimRepository.save({
            it.is(claim) && it.claimStatus == ClaimStatus.UPDATE_ERROR && it.statusMessage.contains('Location')
        })
    }

    def "Error fetching invoice from lookup Location persists UPDATE_ERROR"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = 'https://api.example.com/okonomi/faktura/fakturagrunnlag/systemid/abc'
        ReflectionTestUtils.setField(claimRestService, 'invoiceEndpoint', '/okonomi/faktura/fakturagrunnlag')
        def headers = new HttpHeaders()
        headers.location = URI.create('/invoice/12345')
        restUtil.bodyless(_, _) >> Mono.just(new ResponseEntity<Void>(headers, HttpStatus.FOUND))
        restUtil.get(FakturaResource, '/invoice/12345') >> Mono.error(
                WebClientResponseException.create(500, 'Server Error', HttpHeaders.EMPTY, new byte[0], null))

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        result == null
        1 * claimRepository.save({
            it.is(claim) && it.claimStatus == ClaimStatus.UPDATE_ERROR && it.statusMessage == '500 Server Error'
        })
    }

    def "Update claims should return Mono.empty() when invoiceUri is empty"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)
        claim.setInvoiceUri('') // Set the invoiceUri to an empty string or null

        when:
        def result = claimRestService.updateClaimStatus(claim)

        then:
        0 * restUtil.get(_, _) // Ensure restUtil.get is never called
        result.blockOptional().isEmpty() // Check that the result is Mono.empty()
    }
}
