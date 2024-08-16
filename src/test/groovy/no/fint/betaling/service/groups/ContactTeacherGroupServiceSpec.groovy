package no.fint.betaling.service.groups

import no.fint.betaling.fintdata.*
import no.fint.betaling.group.ContactTeacherGroupService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

class ContactTeacherGroupServiceSpec extends Specification {

    private FintObjectFactory fintObjectFactory
    private ContactTeacherGroupService contactTeacherGroupService
    private SchoolRepository schoolRepository = Mock()
    private ContactTeacherGroupRepository contactTeacherGroupRepository = Mock()
    private ContactTeacherGroupMembershipRepository contactTeacherGroupMembershipRepository = Mock()
    private StudentRelationRepository studentRelationRepository = Mock()
    private StudentRepository studentRepository = Mock()

    void setup() {
        contactTeacherGroupService = new ContactTeacherGroupService(schoolRepository, contactTeacherGroupRepository, contactTeacherGroupMembershipRepository, studentRepository, studentRelationRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Given valid schoolId get customer groups from contact teacher groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def contactTeacherGroup = fintObjectFactory.newContactTeacherGroup()
        def contactTeacherGroupMembership = fintObjectFactory.newContactTeacherGroupMembership()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerGroups = contactTeacherGroupService.getFromContactTeacherGroups('NO123456789')

        then:
        1 * schoolRepository.get() >> [school]
        1 * contactTeacherGroupRepository.getResourceByLink(new Link(verdi: 'link.to.ContactTeacherGroup')) >> Optional.of(contactTeacherGroup)
        1 * contactTeacherGroupMembershipRepository.getResourceByLink(new Link(verdi: 'link.to.ContactTeacherGroupMembership')) >> Optional.of(contactTeacherGroupMembership)
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> Optional.of(studentRelation)
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> Optional.of(student)

        customerGroups.size() == 1
        customerGroups.get(0).name == '3T13DX'
        customerGroups.get(0).customers.get(0).id == '21i3v9'
    }
}
