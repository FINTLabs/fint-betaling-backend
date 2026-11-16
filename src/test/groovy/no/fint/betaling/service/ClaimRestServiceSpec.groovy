package no.fint.betaling.service

import no.fint.betaling.claim.*
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import no.novari.fint.model.resource.okonomi.faktura.FakturagrunnlagResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Sinks
import reactor.core.publisher.Mono
import no.fint.betaling.model.Claim
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
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
        claimRestStatusService.processRequest(claim) >> Mono.fromCallable {
            claim.claimStatus = ClaimStatus.ACCEPTED
            claim
        }

        when:
        def claims = claimRestService.sendClaims([12345L])

        then:
        StepVerifier
                .create(claims)
                .assertNext { c ->
                    assert c.claimStatus == ClaimStatus.ACCEPTED
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
        }) >> Mono.fromCallable {
            claim.claimStatus = ClaimStatus.ACCEPTED
            claim
        }

        response.claimStatus == ClaimStatus.ACCEPTED
        claim.invoiceUri == 'https://link.to.Location'
    }

    def "only twenty of two hundred orders are sent until acceptance or failure releases a slot"() {
        given:
        def claims = (1L..200L).collect { new Claim(orderNumber: it, claimStatus: ClaimStatus.STORED) }
        def gates = new ConcurrentHashMap<Long, Sinks.One<Claim>>()
        def posted = new CopyOnWriteArrayList<Long>()
        def completed = new CopyOnWriteArrayList<Claim>()
        def failures = new CopyOnWriteArrayList<Throwable>()
        claimDatabaseService.getUnsentClaims() >> claims
        invoiceFactory.createInvoice(_) >> new FakturagrunnlagResource()
        restUtil.post(*_) >> Mono.just(new HttpHeaders(location: URI.create('https://example.com/status/pending')))
        claimRestStatusService.processRequest(_ as Claim) >> { Claim claim ->
            posted.add(claim.orderNumber)
            def gate = Sinks.one()
            gates.put(claim.orderNumber, gate)
            gate.asMono()
        }

        when:
        claimRestService.sendClaims(claims*.orderNumber).subscribe(completed::add, failures::add)

        then:
        posted.size() == 20
        gates.size() == 20
        completed.empty

        when:
        (1L..200L).each { orderNumber ->
            assert gates.size() <= 20
            def claim = claims[(int) orderNumber - 1]
            claim.claimStatus = [ClaimStatus.ACCEPTED, ClaimStatus.SEND_ERROR, ClaimStatus.ACCEPT_ERROR][(int) orderNumber % 3]
            assert gates.remove(orderNumber).tryEmitValue(claim) == Sinks.EmitResult.OK
            assert posted.size() == Math.min(200, orderNumber + 20)
        }

        then:
        failures.empty
        completed.size() == 200
        gates.isEmpty()
    }

    def "disconnecting the sender does not cancel pending acceptance or resend completed work"() {
        given:
        def claim = new Claim(orderNumber: 1L, claimStatus: ClaimStatus.STORED)
        def gate = Sinks.one()
        claimDatabaseService.getUnsentClaims() >> [claim]
        invoiceFactory.createInvoice(_) >> new FakturagrunnlagResource()
        claimRestStatusService.processRequest(claim) >> gate.asMono()
        def batch = claimRestService.sendClaims([1L])

        when:
        def subscription = batch.subscribe()
        subscription.dispose()
        claim.claimStatus = ClaimStatus.ACCEPTED
        gate.tryEmitValue(claim)
        def result = batch.collectList().block()

        then:
        result == [claim]
        result[0].claimStatus == ClaimStatus.ACCEPTED
        1 * restUtil.post(*_) >> Mono.just(new HttpHeaders(location: URI.create('https://example.com/status/1')))
    }

    def "a failed POST does not stop the remaining orders"() {
        given:
        def first = new Claim(orderNumber: 1L, claimStatus: ClaimStatus.STORED)
        def second = new Claim(orderNumber: 2L, claimStatus: ClaimStatus.STORED)
        claimDatabaseService.getUnsentClaims() >> [first, second]
        invoiceFactory.createInvoice(_) >> new FakturagrunnlagResource()
        restUtil.post(*_) >>> [Mono.error(new IllegalStateException('Connection failed')),
                             Mono.just(new HttpHeaders(location: URI.create('https://example.com/status/2')))]
        claimRestStatusService.processRequest(second) >> Mono.fromCallable {
            second.claimStatus = ClaimStatus.ACCEPTED
            second
        }

        expect:
        claimRestService.sendClaims([1L, 2L]).collectList().block()*.claimStatus ==
                [ClaimStatus.SEND_ERROR, ClaimStatus.ACCEPTED]
    }

    def "Refresh polls live filtered invoice queries and persists #expectedStatus"() {
        given:
        def server = new MockWebServer()
        server.start()
        def baseUrl = server.url('/').toString()
        def realRestUtil = new RestUtil(WebClient.builder().baseUrl(baseUrl).build())
        realRestUtil.baseUrl = baseUrl
        def realFintClient = new FintClient(realRestUtil, null, null, null)
        def service = new ClaimRestService(realRestUtil, realFintClient, invoiceFactory,
                claimRepository, claimDatabaseService, claimRestStatusService)
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = baseUrl + 'okonomi/faktura/fakturagrunnlag/ordrenummer/12345'
        server.enqueue(new MockResponse().setResponseCode(202)
                .setHeader('Location', '/okonomi/faktura/fakturagrunnlag/status/order-12345'))
        server.enqueue(new MockResponse().setHeader('Content-Type', 'application/json').setBody("""
            {"ordrenummer":{"identifikatorverdi":"12345"},
             "_links":{"self":[{"href":"${claim.invoiceUri}"}],"faktura":[
               {"href":"${baseUrl}okonomi/faktura/faktura/fakturanummer/90001"},
               {"href":"${baseUrl}okonomi/faktura/faktura/fakturanummer/90002"}]}}
        """))
        [firstPaid, secondPaid].eachWithIndex { paid, index ->
            def number = 90001 + index
            server.enqueue(new MockResponse().setResponseCode(202)
                    .setHeader('Location', "/okonomi/faktura/faktura/status/${number}"))
            server.enqueue(new MockResponse().setHeader('Content-Type', 'application/json').setBody("""
                {"_embedded":{"_entries":[{
                 "fakturanummer":{"identifikatorverdi":"${number}"},
                 "betalt":${paid},"kreditert":${credited},"restbelop":250,
                 "dato":"2026-11-01T00:00:00Z","forfallsdato":"2026-12-01T00:00:00Z"}]}}
            """))
        }

        when:
        def result = service.updateClaimStatus(claim).block(java.time.Duration.ofSeconds(15))

        then:
        result.is(claim)
        claim.claimStatus == expectedStatus
        claim.invoiceNumbers == ['90001', '90002'] as Set
        claim.amountDue == 500L
        claim.invoiceDate != null
        claim.paymentDueDate != null
        1 * claimRepository.save(claim)
        server.requestCount == 6
        def requests = (1..6).collect { server.takeRequest() }
        def reads = requests.findAll { !it.getRequestUrl().encodedPath().contains('/status/') }
        reads.size() == 3
        reads.every { it.getHeader('Prefer') == 'respond-async' }
        def queries = reads.findAll { it.getRequestUrl().encodedPath() == '/okonomi/faktura/faktura' }
        queries.collect { it.getRequestUrl().queryParameter('$filter') } as Set ==
                ["fakturanummer eq '90001'", "fakturanummer eq '90002'"] as Set
        requests.findAll { it.getRequestUrl().encodedPath().contains('/status/') }
                .every { it.getHeader('Prefer') == null }

        cleanup:
        server.shutdown()

        where:
        firstPaid | secondPaid | credited | expectedStatus
        true      | true       | false    | ClaimStatus.PAID
        true      | false      | false    | ClaimStatus.ISSUED
        false     | false      | true     | ClaimStatus.CREDITED
    }

    def "Order without invoices remains accepted"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = 'https://api.example.com/fakturagrunnlag/ordrenummer/12345'
        def invoice = new FakturagrunnlagResource()
        restUtil.getLive(FakturagrunnlagResource, claim.invoiceUri) >> Mono.just(invoice)
        fintClient.getSelfLink(invoice) >> Optional.empty()
        fintClient.getFaktura(invoice) >> Mono.just([])
        fintClient.getFakturanummere([]) >> ([] as Set)
        fintClient.getInvoiceDate([]) >> Optional.empty()
        fintClient.getPaymentDueDate([]) >> Optional.empty()
        fintClient.getAmountDue([]) >> Optional.empty()

        expect:
        claimRestService.updateClaimStatus(claim).block().claimStatus == ClaimStatus.ACCEPTED
    }

    def "Refresh failure persists UPDATE_ERROR"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.ACCEPTED)
        claim.invoiceUri = 'https://api.example.com/fakturagrunnlag/ordrenummer/12345'
        restUtil.getLive(FakturagrunnlagResource, claim.invoiceUri) >> Mono.error(new IllegalStateException('Lookup failed'))

        when:
        def result = claimRestService.updateClaimStatus(claim).block()

        then:
        result == null
        1 * claimRepository.save({
            it.is(claim) && it.claimStatus == ClaimStatus.UPDATE_ERROR && it.statusMessage == 'Lookup failed'
        })
    }

    def "Update claims should return Mono.empty() when invoiceUri is empty"() {
        given:
        def claim = betalingObjectFactory.newClaim(12345L, ClaimStatus.SENT)
        claim.setInvoiceUri('') // Set the invoiceUri to an empty string or null

        when:
        def result = claimRestService.updateClaimStatus(claim)

        then:
        0 * restUtil.getLive(_, _) // Ensure restUtil.get is never called
        result.blockOptional().isEmpty() // Check that the result is Mono.empty()
    }
}
