package no.fint.betaling.util

import no.fint.betaling.common.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.personal.PersonalressursResources
import no.fint.model.resource.utdanning.elev.ElevResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class RestUtilSpec extends Specification {
    MockWebServer mockWebServer
    WebClient webClient
    RestUtil restUtil

    void setup() {
        mockWebServer = new MockWebServer()
        mockWebServer.start()
        String baseUrl = mockWebServer.url("/").toString()
        webClient = WebClient.builder().baseUrl(baseUrl).build()

        restUtil = new RestUtil(webClient)
        restUtil.setBaseUrl(baseUrl)
    }

    def cleanup() {
        mockWebServer.shutdown()
    }

    def "Get resource given invalid response throws WebClientResponseException"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(400))

        when:
        def response = restUtil.get(String, '/test')

        then:
        StepVerifier.create(response)
                .expectErrorSatisfies { e ->
                    assert e instanceof WebClientResponseException
                    assert e.getStatusCode() == HttpStatus.BAD_REQUEST
                }
                .verify()
    }

    def "Set resource given invalid response throws WebClientResponseException"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(400))

        when:
        Mono<HttpHeaders> response = restUtil.post('/test', 'ping', String.class)

        then:
        StepVerifier.create(response)
                .expectErrorMatches { e -> e instanceof WebClientResponseException && e.statusCode == HttpStatus.BAD_REQUEST }
                .verify()
    }

    def "Get ElevResource given valid url returns ElevResource"() {
        given:
        mockWebServer.enqueue(new MockResponse()
                .setBody('''{ "elevnummer": { "identifikatorverdi": "87651234" } }''')
                .setHeader('content-type', 'application/json')
                .setResponseCode(200))

        when:
        def result = restUtil.get(ElevResource.class, '/test')

        then:
        StepVerifier.create(result)
                .assertNext { resource ->
                    assert resource.elevnummer.identifikatorverdi == '87651234'
                }
                .verifyComplete()
    }

    def "Post ElevResource given valid url returns valid response entity"() {
        given:
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "http://test.no/1234")
                .setResponseCode(200))
        def elev = new ElevResource(elevnummer: new Identifikator(identifikatorverdi: '12348765'))

        when:
        def response = restUtil.post('/test', elev, ElevResource.class)

        then:
        response.block().getLocation().toString() == "http://test.no/1234"
    }

//    def 'Get updates for a resource'() {
//        given:
//        def uri = '/administrasjon/personal/personalressurs'
//
//        when:
//        def result = restUtil.getUpdates(PersonalressursResources, uri)
//
//        then:
//        result.totalItems == 1
//        1 * restTemplate.getForObject(
//                'http://beta.localhost/administrasjon/personal/personalressurs/last-updated',
//                _
//        ) >> ['lastUpdated': "12345"]
//        1 * restTemplate.getForObject(
//                'http://beta.localhost/administrasjon/personal/personalressurs?sinceTimeStamp=0',
//                _
//        ) >> new PersonalressursResources(embedded: new AbstractCollectionResources.EmbeddedResources<PersonalressursResource>(entries:
//                [new PersonalressursResource(ansattnummer: new Identifikator(identifikatorverdi: '123456'))]))
//
//        when:
//        def result2 = restUtil.getUpdates(PersonalressursResources, uri)
//
//        then:
//        result2.totalItems == 0
//        1 * restTemplate.getForObject(
//                'http://beta.localhost/administrasjon/personal/personalressurs/last-updated',
//                _
//        ) >> ['lastUpdated': "12345"]
//        1 * restTemplate.getForObject(
//                'http://beta.localhost/administrasjon/personal/personalressurs?sinceTimeStamp=12346',
//                _
//        ) >> new PersonalressursResources(embedded: new AbstractCollectionResources.EmbeddedResources<PersonalressursResource>(entries: []))
//
//    }

    def 'Get updates for a resource'() {
        given:
        def uri = '/administrasjon/personal/personalressurs'
        def lastUpdatedResponse = '''{ "lastUpdated": "12345" }'''
        def firstCallResponse = '''{
    "_embedded": {
        "_entries": [
            {
                "ansattnummer": {
                    "identifikatorverdi": "123456"
                }
            }
        ]
    }
}'''
        def secondCallResponse = '''{
        "_embedded": {
            "_entries": []
        }
    }'''

        // Enqueue responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(lastUpdatedResponse)
                .setHeader('content-type', 'application/json')
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setBody(firstCallResponse)
                .setHeader('content-type', 'application/json')
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setBody(lastUpdatedResponse)
                .setHeader('content-type', 'application/json')
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setBody(secondCallResponse)
                .setHeader('content-type', 'application/json')
                .setResponseCode(200))

        when:
        def result = restUtil.getUpdates(PersonalressursResources.class, uri)

        then:
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify()

        when:
        def result2 = restUtil.getUpdates(PersonalressursResources.class, uri)

        then:
        then:
        StepVerifier.create(result2).expectComplete()
    }

}
