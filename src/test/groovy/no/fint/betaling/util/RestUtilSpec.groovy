package no.fint.betaling.util

import no.fint.betaling.common.util.RestUtil
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResources
import no.novari.fint.model.resource.utdanning.elev.ElevResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClientRequestException
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
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

    def "Live read follows relative Location and keeps polling when subsequent 202 has no Location"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(202).setHeader('Location', 'status/read-1'))
        mockWebServer.enqueue(new MockResponse().setResponseCode(202))
        mockWebServer.enqueue(new MockResponse().setHeader('Content-Type', 'application/json')
                .setBody('{"elevnummer":{"identifikatorverdi":"87651234"}}'))

        when:
        def result = restUtil.getLive(ElevResource, '/elev').block(java.time.Duration.ofSeconds(10))

        then:
        result.elevnummer.identifikatorverdi == '87651234'
        mockWebServer.takeRequest().getHeader('Prefer') == 'respond-async'
        mockWebServer.takeRequest().path == '/status/read-1'
        mockWebServer.takeRequest().path == '/status/read-1'
    }

    def "Live read rejects initial 202 without Location"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(202))

        expect:
        StepVerifier.create(restUtil.getLive(ElevResource, '/elev'))
                .expectErrorMatches { it instanceof IllegalStateException && it.message.contains('Location') }
                .verify(java.time.Duration.ofSeconds(5))
    }

    def "Live read preserves HTTP errors from status polling"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(202).setHeader('Location', '/status/read-1'))
        mockWebServer.enqueue(new MockResponse().setResponseCode(500))

        expect:
        StepVerifier.create(restUtil.getLive(ElevResource, '/elev'))
                .expectErrorMatches { it instanceof WebClientResponseException && it.statusCode.value() == 500 }
                .verify(java.time.Duration.ofSeconds(5))
    }

    def "Live read accepts an immediate response but rejects an empty body"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(200))

        expect:
        StepVerifier.create(restUtil.getLive(ElevResource, '/elev'))
                .expectErrorMatches { it instanceof IllegalStateException && it.message.contains('empty body') }
                .verify(java.time.Duration.ofSeconds(5))
    }

    def "live status polling releases its connection before waiting and polling again"() {
        given:
        def pool = ConnectionProvider.builder('live-read-single-connection')
                .maxConnections(1).pendingAcquireTimeout(Duration.ofMillis(250)).build()
        def baseUrl = mockWebServer.url('/').toString()
        def client = WebClient.builder().baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(pool))).build()
        def rest = new RestUtil(client)
        rest.baseUrl = baseUrl
        mockWebServer.enqueue(new MockResponse().setResponseCode(202).setHeader('Location', '/status/read-1'))
        mockWebServer.enqueue(new MockResponse().setResponseCode(202))
        mockWebServer.enqueue(new MockResponse().setHeader('Content-Type', 'application/json')
                .setBody('{"elevnummer":{"identifikatorverdi":"87651234"}}'))

        when:
        def result = rest.getLive(ElevResource, '/elev').block(Duration.ofSeconds(10))

        then:
        result.elevnummer.identifikatorverdi == '87651234'
        mockWebServer.requestCount == 3

        cleanup:
        pool.disposeLater().block(Duration.ofSeconds(5))
    }

    def "live read succeeds after two temporary HTTP #status failures"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(status))
        mockWebServer.enqueue(new MockResponse().setResponseCode(status))
        mockWebServer.enqueue(new MockResponse().setHeader('Content-Type', 'application/json')
                .setBody('{"elevnummer":{"identifikatorverdi":"87651234"}}'))

        when:
        def result = restUtil.getLive(ElevResource, '/elev').block(Duration.ofSeconds(10))

        then:
        result.elevnummer.identifikatorverdi == '87651234'
        mockWebServer.requestCount == 3
        (1..3).collect { mockWebServer.takeRequest() }.every {
            it.method == 'GET' && it.path == '/elev' && it.getHeader('Prefer') == 'respond-async'
        }

        where:
        status << [429, 502, 503, 504]
    }

    def "retrying a failed status GET keeps its URL and does not start another live read"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(202).setHeader('Location', '/status/read-1'))
        mockWebServer.enqueue(new MockResponse().setResponseCode(503))
        mockWebServer.enqueue(new MockResponse().setHeader('Content-Type', 'application/json')
                .setBody('{"elevnummer":{"identifikatorverdi":"87651234"}}'))

        when:
        def result = restUtil.getLive(ElevResource, '/elev').block(Duration.ofSeconds(10))

        then:
        result.elevnummer.identifikatorverdi == '87651234'
        mockWebServer.requestCount == 3
        mockWebServer.takeRequest().path == '/elev'
        def polls = [mockWebServer.takeRequest(), mockWebServer.takeRequest()]
        polls.every { it.path == '/status/read-1' && it.getHeader('Prefer') == null }
    }

    def "network failures stop after three attempts with one and two second retry delays"() {
        given:
        def attempts = new AtomicInteger()
        def failure = new WebClientRequestException(new java.net.ConnectException('Connection timed out'),
                HttpMethod.GET, URI.create('https://example.com/elev'), HttpHeaders.EMPTY)
        def client = WebClient.builder().exchangeFunction { request ->
            attempts.incrementAndGet()
            Mono.error(failure)
        }.build()
        def rest = new RestUtil(client)
        rest.baseUrl = 'https://example.com'

        expect:
        StepVerifier.withVirtualTime { rest.getLive(ElevResource, '/elev') }
                .then { assert attempts.get() == 1 }
                .thenAwait(Duration.ofSeconds(1))
                .then { assert attempts.get() == 2 }
                .thenAwait(Duration.ofSeconds(2))
                .expectErrorMatches { it.is(failure) }
                .verify()
        attempts.get() == 3
    }

    def "permanent HTTP #status failures are not retried"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(status))

        expect:
        StepVerifier.create(restUtil.getLive(ElevResource, '/elev'))
                .expectErrorMatches { it instanceof WebClientResponseException && it.statusCode.value() == status }
                .verify(Duration.ofSeconds(5))
        mockWebServer.requestCount == 1

        where:
        status << [400, 401, 403, 404, 500]
    }

    def "an order POST with a transient HTTP failure is not retried"() {
        given:
        mockWebServer.enqueue(new MockResponse().setResponseCode(503))

        expect:
        StepVerifier.create(restUtil.post('/order', 'order', String))
                .expectErrorMatches { it instanceof WebClientResponseException && it.statusCode.value() == 503 }
                .verify(Duration.ofSeconds(5))
        mockWebServer.requestCount == 1
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
