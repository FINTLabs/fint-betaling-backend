package no.fint.betaling.util

import no.fint.betaling.exception.InvalidResponseException
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.elev.ElevResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.nio.charset.Charset

class RestUtilSpec extends Specification {
    private RestTemplate restTemplate
    private RestUtil restUtil

    void setup() {
        restTemplate = Mock(RestTemplate)
        restUtil = new RestUtil(restTemplate: restTemplate)
    }

    def "Get resource given invalid response throws InvalidResponseException"() {
        when:
        restUtil.get(String, 'http://localhost'.toURI(), 'test.no')

        then:
        1 * restTemplate.exchange('http://localhost'.toURI(), HttpMethod.GET, _ as HttpEntity, _ as Class) >> {
            throw new InvalidResponseException('test', Throwable.newInstance())
        }
        thrown(InvalidResponseException)
    }

    def "Set resource given invalid response throws InvalidResponseException"() {
        when:
        restUtil.post(String, 'http://localhost'.toURI(), 'ping', 'test.no')

        then:
        1 * restTemplate.exchange('http://localhost'.toURI(), HttpMethod.POST, _ as HttpEntity, _ as Class) >> {
            throw new InvalidResponseException('test', Throwable.newInstance())
        }
        thrown(InvalidResponseException)
    }

    def "Get ElevResource given valid url returns ElevResource"() {
        when:
        def resource = restUtil.get(ElevResource, 'http://localhost'.toURI(), 'test.no')

        then:
        1 * restTemplate.exchange('http://localhost'.toURI(), HttpMethod.GET, _ as HttpEntity, _ as Class<ElevResource>) >> ResponseEntity.ok(new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')))
        resource.elevnummer.identifikatorverdi == 'test'
    }

    def "Post ElevResource given valid url returns valid response entity"() {
        when:
        def response = restUtil.post(ElevResource.class, 'http://localhost'.toURI(), new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')), 'test.no')

        then:
        1 * restTemplate.exchange('http://localhost'.toURI(), HttpMethod.POST, _ as HttpEntity, _ as Class<ElevResource>) >> ResponseEntity.ok().build()
        response.statusCode.is2xxSuccessful()
    }
}
