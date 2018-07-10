package no.fint.betaling.service

import no.fint.betaling.model.InvalidResponseException
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.elev.ElevResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestServiceSpec extends Specification {
    private RestTemplate restTemplate
    private RestService restService

    void setup() {
        restTemplate = Mock(RestTemplate)
        restService = new RestService(restTemplate: restTemplate, clientName: 'test-Betaling')
    }

    def "Get resource given invalid response throws InvalidResponseException"() {
        when:
        restService.getResource(String, 'http://localhost', 'test.no')

        then:
        1 * restTemplate.exchange('http://localhost', HttpMethod.GET, _ as HttpEntity, _ as Class) >> {
            throw new RestClientException('test')
        }
        thrown(InvalidResponseException)
    }

    def "Get ElevResource given valid url returns ElevResource"() {
        when:
        def resource = restService.getResource(ElevResource, 'http://localhost', 'test.no')

        then:
        1 * restTemplate.exchange('http://localhost', HttpMethod.GET, _ as HttpEntity, _ as Class<ElevResource>) >> ResponseEntity.ok(new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')))
        resource.elevnummer.identifikatorverdi == 'test'
    }

    def "Post ElevResource given valid url returns valid response entity"() {
        when:
        def response = restService.setResource(ElevResource.class, 'http://localhost', new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')), 'test.no')

        then:
        1 * restTemplate.exchange(_ as String, HttpMethod.POST, _ as HttpEntity, _ as Class<ElevResource>) >> ResponseEntity.ok().build()
        response.statusCode.is2xxSuccessful()
    }
}
