package no.fint.betaling.service

import no.fint.betaling.repository.GroupRepository
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private GroupService groupService
    private GroupRepository groupRepository
    private FintObjectFactory fintObjectFactory

    void setup() {
        groupRepository = Mock()
        groupService = new GroupService(groupRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Given valid schoolId get customer group from school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerGroup = groupService.getCustomerGroupBySchool('NO123456789')

        then:
        1 * groupRepository.getSchools() >> [(new Link(verdi: 'link.to.School')): school]
        1 * groupRepository.getStudentRelations() >> [(new Link(verdi: 'link.to.StudentRelation')): studentRelation]
        1 * groupRepository.getStudents() >> [(new Link(verdi: 'link.to.Student')): student]

        customerGroup.name == 'HVS'
        customerGroup.customers.get(0).id == '21i3v9'
    }

    def "Given valid schoolId get customer groups from contact teacher groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def contactTeacherGroup = fintObjectFactory.newContactTeacherGroup()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerGroups = groupService.getCustomerGroupsByContactTeacherGroupsAndSchool('NO123456789')

        then:
        1 * groupRepository.getSchools() >> [(new Link(verdi: 'link.to.School')): school]
        1 * groupRepository.getContactTeacherGroups() >> [(new Link(verdi: 'link.to.ContactTeacherGroup')): contactTeacherGroup]
        1 * groupRepository.getStudentRelations() >> [(new Link(verdi: 'link.to.StudentRelation')): studentRelation]
        1 * groupRepository.getStudents() >> [(new Link(verdi: 'link.to.Student')): student]

        customerGroups.size() == 1
        customerGroups.get(0).name == '3T13DX'
        customerGroups.get(0).customers.get(0).id == '21i3v9'
    }

    def "Given valid schoolId get customer groups from teaching groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def teachingGroup = fintObjectFactory.newTeachingGroup()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerList = groupService.getCustomerGroupsByTeachingGroupsAndSchool(school.organisasjonsnummer.identifikatorverdi)

        then:
        1 * groupRepository.getSchools() >> [(new Link(verdi: 'link.to.School')): school]
        1 * groupRepository.getTeachingGroups() >> [(new Link(verdi: 'link.to.TeachingGroup')): teachingGroup]
        1 * groupRepository.getStudentRelations() >> [(new Link(verdi: 'link.to.StudentRelation')): studentRelation]
        1 * groupRepository.getStudents() >> [(new Link(verdi: 'link.to.Student')): student]

        customerList.size() == 1
        customerList.get(0).name == 'YFF4106'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }

    def "Given valid schoolId get customer groups from basis groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def basisGroup = fintObjectFactory.newBasisGroup()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerList = groupService.getCustomerGroupsByBasisGroupsAndSchool('NO123456789')

        then:
        1 * groupRepository.getSchools() >> [(new Link(verdi: 'link.to.School')): school]
        1 * groupRepository.getBasisGroups() >> [(new Link(verdi: 'link.to.BasisGroup')): basisGroup]
        1 * groupRepository.getStudentRelations() >> [(new Link(verdi: 'link.to.StudentRelation')): studentRelation]
        1 * groupRepository.getStudents() >> [(new Link(verdi: 'link.to.Student')): student]

        customerList.size() == 1
        customerList.get(0).name == '1TIA'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }
}
