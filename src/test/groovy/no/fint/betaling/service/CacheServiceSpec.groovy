package no.fint.betaling.service

import org.codehaus.jackson.map.ObjectMapper
import spock.lang.Specification

class CacheServiceSpec extends Specification {
    private RestService restService
    private CacheService cacheService
    private ObjectMapper objectMapper

    void setup() {
        restService = Mock()
        cacheService = new CacheService(restService: restService)
        objectMapper = new ObjectMapper()
    }

    def "Full fetch first"() {
        given:
        def json = objectMapper.readValue("{ \"lastUpdated\": 123 }", Map)

        when:
        def result = cacheService.getUpdates(String, "http://foo", "mock.no")
        println(result)

        then:
        result == "Monkey"
        1 * restService.getResource(_, "http://foo/last-updated", "mock.no") >> json
        1 * restService.getResource(_, "http://foo?sinceTimeStamp=0", "mock.no") >> "Monkey"
    }

    def "Second fetch with since"() {
        given:
        def json = objectMapper.readValue("{ \"lastUpdated\": 123 }", Map)

        when:
        def result = cacheService.getUpdates(String, "http://foo", "mock.no")
        println(result)
        result = cacheService.getUpdates(String, "http://foo", "mock.no")
        println(result)

        then:
        result == "Gorilla"
        2 * restService.getResource(_, "http://foo/last-updated", "mock.no") >> json
        1 * restService.getResource(_, "http://foo?sinceTimeStamp=0", "mock.no") >> "Monkey"
        1 * restService.getResource(_, "http://foo?sinceTimeStamp=124", "mock.no") >> "Gorilla"
    }
}
