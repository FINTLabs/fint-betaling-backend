package no.fint.betaling.service.groups

import no.fint.betaling.fintdata.ContactTeacherGroupMembershipRepository
import no.fint.betaling.fintdata.ContactTeacherGroupRepository
import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.fintdata.StudentRelationRepository
import no.fint.betaling.fintdata.StudentRepository
import no.fint.betaling.fintdata.TeachingGroupMembershipRepository
import no.fint.betaling.fintdata.TeachingGroupRepository
import no.fint.betaling.group.ContactTeacherGroupService
import no.fint.betaling.group.TeachingGroupService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

class TeachingGroupServiceSpec extends Specification {

    private FintObjectFactory fintObjectFactory
    private TeachingGroupService teachingGroupService
    private SchoolRepository schoolRepository = Mock()
    private TeachingGroupRepository teachingGroupRepository = Mock()
    private TeachingGroupMembershipRepository teachingGroupMembershipRepository = Mock()
    private StudentRelationRepository studentRelationRepository = Mock()
    private StudentRepository studentRepository = Mock()

    void setup() {
        teachingGroupService = new TeachingGroupService(schoolRepository, teachingGroupRepository, teachingGroupMembershipRepository, studentRepository, studentRelationRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Given valid schoolId get customer groups from teaching groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def teachingGroup = fintObjectFactory.newTeachingGroup()
        def teachingGroupMembership = fintObjectFactory.newTeachingGroupMembership()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerList = teachingGroupService.getFromTeachingGroups(school.organisasjonsnummer.identifikatorverdi)

        then:
        1 * schoolRepository.get() >> [school]
        1 * teachingGroupRepository.getResourceByLink(new Link(verdi: 'link.to.TeachingGroup')) >> Optional.of(teachingGroup)
        1 * teachingGroupMembershipRepository.getResourceByLink(new Link(verdi: 'link.to.TeachingGroupMembership')) >> Optional.of(teachingGroupMembership)
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> Optional.of(studentRelation)
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> Optional.of(student)

        customerList.size() == 1
        customerList.get(0).name == 'YFF4106'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }
}
