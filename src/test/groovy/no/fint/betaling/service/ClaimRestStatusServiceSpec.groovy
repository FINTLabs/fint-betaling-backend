package no.fint.betaling.service

import no.fint.betaling.claim.ClaimRepository
import no.fint.betaling.claim.ClaimRestStatusService
import no.fint.betaling.common.exception.ClientErrorException
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification

class ClaimRestStatusServiceSpec extends Specification {
    RestUtil restUtil = Mock()
    ClaimRepository claimRepository = Mock()
    ClaimRestStatusService claimRestStatusService = new ClaimRestStatusService(restUtil, claimRepository)

    def setup() {
        claimRestStatusService = new ClaimRestStatusService(restUtil, claimRepository)
    }

    def "processRequest should update claim on success"() {
        given:
        def claim = new Claim(orderNumber: 12345L, invoiceUri: "http://test.com/status/123")
        def headers = new HttpHeaders()
        headers.setLocation(URI.create("http://test.com/newLocation"))
        def response = new ResponseEntity<Void>(headers, HttpStatus.CREATED)


        when:
        claimRestStatusService.processRequest(claim)

        then:
        1 * restUtil.bodyless("http://test.com/status/123") >> Mono.just(response)
        1 * claimRepository.save(_ as Claim) >> { Claim savedClaim ->
            assert savedClaim.claimStatus == ClaimStatus.ACCEPTED
            assert savedClaim.invoiceUri == "http://test.com/newLocation"
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
        def claim1 = new Claim(claimStatus: ClaimStatus.SENT)
        def claim2 = new Claim(claimStatus: ClaimStatus.ACCEPTED)
        def claim3 = new Claim(claimStatus: ClaimStatus.ERROR, statusMessage: "Custom error message")

        when:
        claimRestStatusService.setStatusMessages([claim1, claim2, claim3])

        then:
        assert claim1.statusMessage == "Overføring pågår"
        assert claim2.statusMessage == "Overført til økonomisystem"
        assert claim3.statusMessage == "Custom error message" // Should not overwrite existing message
    }
}
