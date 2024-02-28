package no.fint.betaling.repository

import no.fint.betaling.config.Endpoints
import no.fint.betaling.util.FintObjectFactory
import no.fint.betaling.util.RestUtil
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources
import reactor.core.publisher.Mono
import spock.lang.Ignore
import spock.lang.Specification

class GroupRepositorySpec extends Specification {
    private Endpoints endpoints
    private RestUtil restUtil
    private GroupRepository groupRepository
    private FintObjectFactory fintObjectFactory

    void setup() {
        endpoints = new Endpoints()
        restUtil = Mock()
        fintObjectFactory = new FintObjectFactory()
        groupRepository = new GroupRepository(restUtil, endpoints)
    }

    def "Update schools on update all"() {
        given:
        def resources = new SkoleResources()
        resources.addResource(fintObjectFactory.newSchool())

        when:
        groupRepository.updateAll()

        then:
        1 * restUtil.getWithRetry(_, endpoints.getSchool()) >> Mono.just(resources)
        _ * restUtil.getWithRetry(_, _) >> Mono.empty()
    }

    def "Get all schools"() {
        given:
        def resources = new SkoleResources()
        resources.addResource(fintObjectFactory.newSchool())

        when:
        def map = groupRepository.getSchools()

        then:
        1 * restUtil.getWithRetry(_ as Class<SkoleResources>, _) >> Mono.just(resources)
        map.size() == 1
        map.get(Link.with('link.to.School')).navn == 'HVS'
    }
}
