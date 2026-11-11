package no.fint.betaling.service

import no.fint.betaling.claim.ClaimRepository
import no.fint.betaling.claim.ClaimRestStatusService
import no.fint.betaling.common.exception.ClientErrorException
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.OrderItem
import no.fint.betaling.model.dto.ClaimDto
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.scheduler.VirtualTimeScheduler
import spock.lang.Specification

import java.time.Duration

class ClaimRestStatusServiceSpec extends Specification {
    RestUtil restUtil = Mock()
    ClaimRepository claimRepository = Mock()
    ClaimRestStatusService claimRestStatusService = new ClaimRestStatusService(restUtil, claimRepository)

    def setup() {
        claimRestStatusService = new ClaimRestStatusService(restUtil, claimRepository)
    }

    def "processRequest should follow Location from lookup response #lookupStatus"() {
        given:
        def claim = new Claim(orderNumber: 12345L, invoiceUri: "http://test.com/invoices?filter=12345")
        def headers = new HttpHeaders()
        headers.setLocation(URI.create("/status/123"))
        def response = new ResponseEntity<Void>(headers, lookupStatus)
        def finalHeaders = new HttpHeaders()
        if (finalLocation) {
            finalHeaders.setLocation(URI.create(finalLocation))
        }

        when:
        claimRestStatusService.processRequest(claim)

        then:
        1 * restUtil.bodyless("http://test.com/invoices?filter=12345") >> Mono.just(response)
        1 * restUtil.bodyless("http://test.com/status/123") >> Mono.just(new ResponseEntity<Void>(finalHeaders, HttpStatus.OK))
        1 * claimRepository.save(_ as Claim) >> { Claim savedClaim ->
            assert savedClaim.claimStatus == ClaimStatus.ACCEPTED
            assert savedClaim.invoiceUri == expectedLocation
        }

        where:
        lookupStatus        | finalLocation                 | expectedLocation
        HttpStatus.ACCEPTED  | "http://test.com/newLocation" | "http://test.com/newLocation"
        HttpStatus.CREATED   | "/newLocation"                | "http://test.com/newLocation"
        HttpStatus.FOUND     | null                          | "http://test.com/status/123"
        HttpStatus.OK        | null                          | "http://test.com/status/123"
    }

    def "processRequest should poll the followed URL while it returns accepted"() {
        given:
        def claim = new Claim(orderNumber: 12345L, invoiceUri: "http://test.com/invoices", claimStatus: ClaimStatus.SENT)
        def headers = new HttpHeaders()
        headers.setLocation(URI.create("http://test.com/status/123"))
        def scheduler = VirtualTimeScheduler.getOrSet()
        int attempts = 0
        restUtil.bodyless(claim.invoiceUri) >> Mono.just(new ResponseEntity<Void>(headers, HttpStatus.ACCEPTED))
        restUtil.bodyless("http://test.com/status/123") >> Mono.defer {
            attempts++
            Mono.just(new ResponseEntity<Void>(new HttpHeaders(), attempts == 1 ? HttpStatus.ACCEPTED : HttpStatus.OK))
        }

        when:
        claimRestStatusService.processRequest(claim)

        then:
        attempts == 1
        claim.claimStatus == ClaimStatus.SENT
        0 * claimRepository.save(_)

        when:
        scheduler.advanceTimeBy(Duration.ofSeconds(10))

        then:
        attempts == 2
        1 * claimRepository.save(_ as Claim) >> { Claim savedClaim ->
            assert savedClaim.claimStatus == ClaimStatus.ACCEPTED
            assert savedClaim.invoiceUri == "http://test.com/status/123"
        }

        cleanup:
        VirtualTimeScheduler.reset()
    }

    def "processRequest should handle missing Location"() {
        given:
        def claim = new Claim(orderNumber: 12345L, invoiceUri: "http://test.com/invoices")

        when:
        claimRestStatusService.processRequest(claim)

        then:
        1 * restUtil.bodyless(claim.invoiceUri) >> Mono.just(new ResponseEntity<Void>(new HttpHeaders(), HttpStatus.OK))
        1 * claimRepository.save(_ as Claim) >> { Claim savedClaim ->
            assert savedClaim.claimStatus == ClaimStatus.SEND_ERROR
            assert savedClaim.statusMessage.contains("Location is empty")
        }
        0 * restUtil.bodyless(_)
    }

    def "processRequest should handle errors from the followed URL"() {
        given:
        def claim = new Claim(orderNumber: 12345L, invoiceUri: "http://test.com/invoices")
        def headers = new HttpHeaders()
        headers.setLocation(URI.create("http://test.com/status/123"))

        when:
        claimRestStatusService.processRequest(claim)

        then:
        1 * restUtil.bodyless("http://test.com/invoices") >> Mono.just(new ResponseEntity<Void>(headers, HttpStatus.ACCEPTED))
        1 * restUtil.bodyless("http://test.com/status/123") >> Mono.error(new ClientErrorException(HttpStatus.BAD_REQUEST))
        1 * claimRepository.save(_ as Claim) >> { Claim savedClaim ->
            assert savedClaim.claimStatus == ClaimStatus.SEND_ERROR
            assert savedClaim.statusMessage.contains("400 BAD_REQUEST")
        }
    }

    def "processRequest should handle client error"() {
        given:
        def claim = new Claim(orderNumber: 12345L, invoiceUri: "http://test.com/status/123")
        def response = new ResponseEntity<Void>(new HttpHeaders(), HttpStatus.BAD_REQUEST)

        when:
        claimRestStatusService.processRequest(claim)

        then:
        1 * restUtil.bodyless("http://test.com/status/123") >> Mono.error(new ClientErrorException(HttpStatus.BAD_REQUEST))
        1 * claimRepository.save(_ as Claim) >> { Claim savedClaim ->
            assert savedClaim.claimStatus == ClaimStatus.SEND_ERROR
            assert savedClaim.statusMessage.contains("400 BAD_REQUEST")
        }
    }

    def "setStatusMessages should set correct messages for claims"() {
        given:
        def claim1 = new Claim()
        claim1.setOrderItems(List.of(new OrderItem()))
        claim1.setClaimStatus(ClaimStatus.SENT)
        def claimDto1 = new ClaimDto(claim1)
        def claim2 = new Claim()
        claim2.setOrderItems(List.of(new OrderItem()))
        claim2.setClaimStatus(ClaimStatus.ACCEPTED)
        def claimDto2 = new ClaimDto(claim2)
        def claim3 = new Claim()
        claim3.setOrderItems(List.of(new OrderItem()))
        claim3.setClaimStatus(ClaimStatus.ERROR)
        claim3.setStatusMessage("Custom error message")
        def claimDto3 = new ClaimDto(claim3)

        when:
        claimRestStatusService.setStatusMessages([claimDto1, claimDto2, claimDto3])

        then:
        assert claimDto1.statusMessage == "Overføring pågår"
        assert claimDto2.statusMessage == "Overført til økonomisystem"
        assert claimDto3.statusMessage == "Custom error message" // Should not overwrite existing message
    }
}
