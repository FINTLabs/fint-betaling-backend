package no.fint.betaling.util

import no.fint.betaling.exception.InvalidResponseException
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.AbstractCollectionResources
import no.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.fint.model.resource.administrasjon.personal.PersonalressursResources
import no.fint.model.resource.utdanning.elev.ElevResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestUtilSpec extends Specification {
    private RestTemplate restTemplate
    private RestUtil restUtil

    void setup() {
        restTemplate = Mock(RestTemplate)
        restUtil = new RestUtil(restTemplate: restTemplate)
    }

    def "Get resource given invalid response throws InvalidResponseException"() {
        when:
        restUtil.get(String, 'http://localhost')

        then:
        1 * restTemplate.getForObject('http://localhost', _ as Class<String>) >> {
            throw new InvalidResponseException(HttpStatus.BAD_REQUEST, 'test', Throwable.newInstance())
        }
        thrown(InvalidResponseException)
    }

    def "Set resource given invalid response throws InvalidResponseException"() {
        when:
        restUtil.post(String, 'http://localhost'.toURI(), 'ping')

        then:
        1 * restTemplate.postForEntity('http://localhost'.toURI(), _, _ as Class) >> {
            throw new InvalidResponseException(HttpStatus.BAD_REQUEST, 'test', Throwable.newInstance())
        }
        thrown(InvalidResponseException)
    }

    def "Get ElevResource given valid url returns ElevResource"() {
        when:
        def resource = restUtil.get(ElevResource, 'http://localhost')

        then:
        1 * restTemplate.getForObject('http://localhost', _ as Class<ElevResource>) >> new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test'))
        resource.elevnummer.identifikatorverdi == 'test'
    }

    def "Post ElevResource given valid url returns valid response entity"() {
        when:
        def response = restUtil.post(ElevResource.class, 'http://localhost'.toURI(), new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')))

        then:
        1 * restTemplate.postForEntity('http://localhost'.toURI(), _, _ as Class<ElevResource>) >> ResponseEntity.ok().build()
        response.statusCode.is2xxSuccessful()
    }

    def 'Get updates for a resource'() {
        given:
        def uri = 'https://play-with-fint.felleskomponent.no/administrasjon/personal/personalressurs'

        when:
        def result = restUtil.getUpdates(PersonalressursResources, uri)

        then:
        result.totalItems == 1
        1 * restTemplate.getForObject(
                'https://play-with-fint.felleskomponent.no/administrasjon/personal/personalressurs/last-updated',
                _
        ) >> ['lastUpdated': "12345"]
        1 * restTemplate.getForObject(
                'https://play-with-fint.felleskomponent.no/administrasjon/personal/personalressurs?sinceTimeStamp=0',
                _
        ) >> new PersonalressursResources(embedded: new AbstractCollectionResources.EmbeddedResources<PersonalressursResource>(entries:
                [new PersonalressursResource(ansattnummer: new Identifikator(identifikatorverdi: '123456'))]))

        when:
        def result2 = restUtil.getUpdates(PersonalressursResources, uri)

        then:
        result2.totalItems == 0
        1 * restTemplate.getForObject(
                'https://play-with-fint.felleskomponent.no/administrasjon/personal/personalressurs/last-updated',
                _
        ) >> ['lastUpdated': "12345"]
        1 * restTemplate.getForObject(
                'https://play-with-fint.felleskomponent.no/administrasjon/personal/personalressurs?sinceTimeStamp=12346',
                _
        ) >> new PersonalressursResources(embedded: new AbstractCollectionResources.EmbeddedResources<PersonalressursResource>(entries: []))

    }
}
