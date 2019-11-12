package no.fint.betaling.service

import no.fint.betaling.util.RestUtil
import org.codehaus.jackson.map.ObjectMapper
import spock.lang.Ignore
import spock.lang.Specification

class CacheServiceSpec extends Specification {
    private RestUtil restUtil
    private CacheService cacheService
    private ObjectMapper objectMapper

    void setup() {
        restUtil = Mock(RestUtil)
        cacheService = new CacheService(restUtil: restUtil)
        objectMapper = new ObjectMapper()
    }

    @Ignore
    def "Full fetch first"() {
        given:
        def json = objectMapper.readValue("{ \"lastUpdated\": 123 }", Map)

        when:
        def result = cacheService.getUpdates(String, "http://foo", "mock.no")
        println(result)

        then:
        result == "Monkey"
        1 * restUtil.get(_, "http://foo/last-updated", "mock.no") >> json
        1 * restUtil.get(_, "http://foo?sinceTimeStamp=0", "mock.no") >> "Monkey"
    }

    @Ignore
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
        2 * restUtil.get(_, "http://foo/last-updated", "mock.no") >> json
        1 * restUtil.get(_, "http://foo?sinceTimeStamp=0", "mock.no") >> "Monkey"
        1 * restUtil.get(_, "http://foo?sinceTimeStamp=124", "mock.no") >> "Gorilla"
    }
}
