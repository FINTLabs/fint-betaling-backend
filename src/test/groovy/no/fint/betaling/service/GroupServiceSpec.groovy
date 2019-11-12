package no.fint.betaling.service

import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.utdanning.elev.BasisgruppeResources
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResources
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private GroupService groupService
    private CacheService cacheService
    private FintObjectFactory fintObjectFactory

    void setup() {
        cacheService = Mock()
        groupService = new GroupService(cacheService)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Get customer group from school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerGroup = groupService.getCustomerGroupBySchool(school.organisasjonsnummer.identifikatorverdi)

        then:
        1 * cacheService.getResources("schools") >> [(school.organisasjonsnummer.identifikatorverdi): school]
        1 * cacheService.getResources("studentRelations") >> [(studentRelation.selfLinks.get(0)): studentRelation]
        1 * cacheService.getResources("students") >> [(student.elev.get(0)): student]

        customerGroup.name == 'HVS'
        customerGroup.customers.get(0).id == '21i3v9'
    }

    def "Get customer groups from contact teacher groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def contactTeacherGroup = fintObjectFactory.newContactTeacherGroup()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        def contactTeacherGroups = new KontaktlarergruppeResources()
        contactTeacherGroups.addResource(contactTeacherGroup)

        when:
        def customerGroups = groupService.getCustomerGroupsByContactTeacherGroupsAndSchool(school.organisasjonsnummer.identifikatorverdi)

        then:
        1 * cacheService.getResources("schools") >> [(school.organisasjonsnummer.identifikatorverdi): school]
        1 * cacheService.getResources("contactTeacherGroups") >> [(contactTeacherGroup.skole.get(0)): contactTeacherGroups.content]
        1 * cacheService.getResources("studentRelations") >> [(studentRelation.selfLinks.get(0)): studentRelation]
        1 * cacheService.getResources("students") >> [(student.elev.get(0)): student]

        customerGroups.size() == 1
        customerGroups.get(0).name == '3T13DX'
        customerGroups.get(0).customers.get(0).id == '21i3v9'
    }

    def "Get customer groups from teaching groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def teachingGroup = fintObjectFactory.newTeachingGroup()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        def teachingGroups = new UndervisningsgruppeResources()
        teachingGroups.addResource(teachingGroup)

        when:
        def customerList = groupService.getCustomerGroupsByTeachingGroupsAndSchool(school.organisasjonsnummer.identifikatorverdi)

        then:
        1 * cacheService.getResources("schools") >> [(school.getOrganisasjonsnummer().getIdentifikatorverdi()): school]
        1 * cacheService.getResources("teachingGroups") >> [(teachingGroup.skole.get(0)): teachingGroups.content]
        1 * cacheService.getResources("studentRelations") >> [(studentRelation.selfLinks.get(0)): studentRelation]
        1 * cacheService.getResources("students") >> [(student.elev.get(0)): student]

        customerList.size() == 1
        customerList.get(0).name == 'YFF4106'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }

    def "Get customer groups from basis groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def basisGroup = fintObjectFactory.newBasisGroup()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        def basisGroups = new BasisgruppeResources()
        basisGroups.addResource(basisGroup)

        when:
        def customerList = groupService.getCustomerGroupsByBasisGroupsAndSchool(school.organisasjonsnummer.identifikatorverdi)

        then:
        1 * cacheService.getResources("schools") >> [(school.organisasjonsnummer.identifikatorverdi): school]
        1 * cacheService.getResources("basisGroups") >> [(basisGroup.skole.get(0)): basisGroups.content]
        1 * cacheService.getResources("studentRelations") >> [(studentRelation.selfLinks.get(0)): studentRelation]
        1 * cacheService.getResources("students") >> [(student.elev.get(0)): student]

        customerList.size() == 1
        customerList.get(0).name == '1TIA'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }
}
