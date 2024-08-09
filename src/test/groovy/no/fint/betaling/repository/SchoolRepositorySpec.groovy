package no.fint.betaling.repository

import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.util.FintObjectFactory
import no.fint.betaling.common.util.RestUtil
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification

class SchoolRepositorySpec extends Specification {
    private Endpoints endpoints
    private RestUtil restUtil
    private FintObjectFactory fintObjectFactory
    private SchoolRepository schoolRepository

    void setup() {
        endpoints = new Endpoints()
        restUtil = Mock()
        fintObjectFactory = new FintObjectFactory()
        schoolRepository = new SchoolRepository(restUtil, endpoints)
    }

    def "Update schools on update all"() {
        given:
        def resources = new SkoleResources()
        resources.addResource(fintObjectFactory.newSchool())

        when:
        schoolRepository.update()

        then:
        1 * restUtil.getWithRetry(_, endpoints.getSchool()) >> Mono.just(resources)
    }

    def "Get all schools"() {
        given:
        def resources = new SkoleResources()
        resources.addResource(fintObjectFactory.newSchool())

        when:
        def map = schoolRepository.getMap()

        then:
        1 * restUtil.getWithRetry(_ as Class<SkoleResources>, _) >> Mono.just(resources)
        map.size() == 1
        map.get(Link.with('link.to.School')).navn == 'HVS'
    }

    def "Test that it is empty on startup"() {
        expect:
        schoolRepository.isEmpty()
    }

    def "Handle empty response correctly"() {
        given:
        def emptyResources = new SkoleResources()

        when:
        def updates = schoolRepository.update()

        then:
        updates == 0
        1 * restUtil.getWithRetry(_ as Class<SkoleResources>, _) >> Mono.just(emptyResources)
        schoolRepository.isEmpty()
    }


    def "Test return on exception"() {
        when:
        def updates = schoolRepository.update()

        then:
        updates == 0
        1 * restUtil.getWithRetry(_, endpoints.getSchool()) >> Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null))
    }

    def "Handle duplicate resources correctly"() {
        given:
        def resources = new SkoleResources()
        def school = fintObjectFactory.newSchool()
        resources.addResource(school)
        resources.addResource(school) // Duplicate

        when:
        def distinctSchools = schoolRepository.getDistinctSchools()

        then:
        1 * restUtil.getWithRetry(_ as Class<SkoleResources>, _) >> Mono.just(resources)
        distinctSchools.size() == 1
    }

    def "Retrieve specific resource by link"() {
        given:
        def resources = new SkoleResources()
        def school = fintObjectFactory.newSchool()
        def link = Link.with('link.to.School')
        resources.addResource(school)

        when:
        schoolRepository.update()
        def retrievedResource = schoolRepository.getResourceByLink(link)

        then:
        1 * restUtil.getWithRetry(_ as Class<SkoleResources>, _) >> Mono.just(resources)
        retrievedResource.isPresent()
        retrievedResource.get() == school
    }


}
