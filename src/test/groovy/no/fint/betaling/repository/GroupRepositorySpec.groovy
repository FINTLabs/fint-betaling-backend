package no.fint.betaling.repository

import no.fint.betaling.util.FintObjectFactory
import no.fint.betaling.util.RestUtil
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources
import spock.lang.Ignore
import spock.lang.Specification

class GroupRepositorySpec extends Specification {
    private RestUtil restUtil
    private GroupRepository groupRepository
    private FintObjectFactory fintObjectFactory

    void setup() {
        restUtil = Mock()
        fintObjectFactory = new FintObjectFactory()
        groupRepository = new GroupRepository(restUtil, endpoints)
    }

    @Ignore
    def "Update schools"() {
        given:
        def resources = new SkoleResources()
        resources.addResource(fintObjectFactory.newSchool())

        when:
        def map = groupRepository.updateSchools()

        then:
        1 * restUtil.getUpdates(_ as Class<SkoleResources>, _) >> resources
        map.size() == 1
        map.get(Link.with('link.to.School')).navn == 'HVS'
    }

    @Ignore
    def "Get all schools"() {
        given:
        def resources = new SkoleResources()
        resources.addResource(fintObjectFactory.newSchool())

        when:
        def map = groupRepository.getSchools()

        then:
        1 * restUtil.getUpdates(_ as Class<SkoleResources>, _) >> resources
        map.size() == 1
        map.get(Link.with('link.to.School')).navn == 'HVS'
    }
}
