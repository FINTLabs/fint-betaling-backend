package no.fint.betaling.service

import no.fint.betaling.model.InvalidResponseException
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.elev.BasisgruppeResources
import no.fint.model.resource.utdanning.elev.ElevResource
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestServiceSpec extends Specification {
    private RestTemplate restTemplate
    private RestService restService

    void setup() {
        restTemplate = Mock(RestTemplate)
        restService = new RestService(restTemplate: restTemplate)
    }

    def "Get BasisgruppeResources given invalid response throws InvalidResponseExcepion"() {
        when:
        restService.getBasisgruppeResources()

        then:
        1 * restTemplate.exchange(_, _, _, BasisgruppeResources) >> { throw new RestClientException('test') }
        thrown(InvalidResponseException)
    }

    def "Get ElevResource given valid url returns ElevResource"() {
        when:
        def resource = restService.getElevResource('valid.url')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok(new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')))
        resource.elevnummer.identifikatorverdi == 'test'
    }
}
