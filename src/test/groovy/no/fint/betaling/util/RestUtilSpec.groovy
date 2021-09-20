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
        restUtil = new RestUtil(restTemplate: restTemplate, environment: "beta", urlTemplate: "http://%s.localhost%s")
    }

    def "Get resource given invalid response throws InvalidResponseException"() {
        when:
        restUtil.get(String, '/test')

        then:
        1 * restTemplate.getForObject('http://beta.localhost/test', _ as Class<String>) >> {
            throw new InvalidResponseException(HttpStatus.BAD_REQUEST, 'test', Throwable.newInstance())
        }
        thrown(InvalidResponseException)
    }

    def "Set resource given invalid response throws InvalidResponseException"() {
        when:
        restUtil.post('/test', 'ping')

        then:
        1 * restTemplate.postForLocation('http://beta.localhost/test', _) >> {
            throw new InvalidResponseException(HttpStatus.BAD_REQUEST, 'test', Throwable.newInstance())
        }
        thrown(InvalidResponseException)
    }

    def "Get ElevResource given valid url returns ElevResource"() {
        when:
        def resource = restUtil.get(ElevResource, '/test')

        then:
        1 * restTemplate.getForObject('http://beta.localhost/test', _ as Class<ElevResource>) >> new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test'))
        resource.elevnummer.identifikatorverdi == 'test'
    }

    def "Post ElevResource given valid url returns valid response entity"() {
        when:
        def response = restUtil.post('/test', new ElevResource(elevnummer: new Identifikator(identifikatorverdi: 'test')))

        then:
        1 * restTemplate.postForLocation('http://beta.localhost/test', _ as ElevResource) >> new URI('link.to.Result')
        response
    }

    def 'Get updates for a resource'() {
        given:
        def uri = '/administrasjon/personal/personalressurs'

        when:
        def result = restUtil.getUpdates(PersonalressursResources, uri)

        then:
        result.totalItems == 1
        1 * restTemplate.getForObject(
                'http://beta.localhost/administrasjon/personal/personalressurs/last-updated',
                _
        ) >> ['lastUpdated': "12345"]
        1 * restTemplate.getForObject(
                'http://beta.localhost/administrasjon/personal/personalressurs?sinceTimeStamp=0',
                _
        ) >> new PersonalressursResources(embedded: new AbstractCollectionResources.EmbeddedResources<PersonalressursResource>(entries:
                [new PersonalressursResource(ansattnummer: new Identifikator(identifikatorverdi: '123456'))]))

        when:
        def result2 = restUtil.getUpdates(PersonalressursResources, uri)

        then:
        result2.totalItems == 0
        1 * restTemplate.getForObject(
                'http://beta.localhost/administrasjon/personal/personalressurs/last-updated',
                _
        ) >> ['lastUpdated': "12345"]
        1 * restTemplate.getForObject(
                'http://beta.localhost/administrasjon/personal/personalressurs?sinceTimeStamp=12346',
                _
        ) >> new PersonalressursResources(embedded: new AbstractCollectionResources.EmbeddedResources<PersonalressursResource>(entries: []))

    }
}
