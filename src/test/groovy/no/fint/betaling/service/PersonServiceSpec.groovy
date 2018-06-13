package no.fint.betaling.service

import no.fint.betaling.model.InvalidResponseException
import no.fint.model.resource.felles.PersonResource
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class PersonServiceSpec extends Specification {

    private RestTemplate restTemplate
    private PersonService personService

    void setup() {
        restTemplate = Mock(RestTemplate)
        personService = new PersonService(restTemplate: restTemplate)
    }

    def "Get person given invalid url throws InvalidResponseException"() {
        when:
        personService.getPerson('invalid.url')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> { throw new RestClientException('test exception') }
        thrown(InvalidResponseException)
    }

    def "Get person given valid url"() {
        when:
        def person = personService.getPerson('http://localhost/person')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok(new PersonResource())
        person
    }
}
