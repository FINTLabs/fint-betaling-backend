package no.fint.betaling.service

import no.fint.betaling.fintdata.BasisGroupRepository
import no.fint.betaling.fintdata.ContactTeacherGroupRepository
import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.fintdata.StudentRelationRepository
import no.fint.betaling.fintdata.StudentRepository
import no.fint.betaling.fintdata.TeachingGroupRepository
import no.fint.betaling.group.GroupService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.felles.kompleksedatatyper.Periode
import no.fint.model.resource.Link
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private GroupService groupService
    private FintObjectFactory fintObjectFactory
    private SchoolRepository schoolRepository;
    private TeachingGroupRepository teachingGroupRepository;
    private StudentRelationRepository studentRelationRepository;
    private StudentRepository studentRepository;
    private BasisGroupRepository basisGroupRepository;
    private ContactTeacherGroupRepository contactTeacherGroupRepository;

    void setup() {
        schoolRepository = Mock()
        teachingGroupRepository = Mock()
        studentRelationRepository = Mock()
        studentRepository = Mock()
        basisGroupRepository = Mock()
        contactTeacherGroupRepository = Mock()
        groupService = new GroupService(schoolRepository, teachingGroupRepository, studentRelationRepository, studentRepository, basisGroupRepository, contactTeacherGroupRepository)
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
        1 * schoolRepository.get() >> [school]
        1 * studentRelationRepository.getResourceByLink(_) >> studentRelation
        1 * studentRepository.getResourceByLink(_) >> student

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
        1 * schoolRepository.get() >> [school]
        1 * contactTeacherGroupRepository.getResourceByLink(new Link(verdi: 'link.to.ContactTeacherGroup')) >> contactTeacherGroup
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> studentRelation
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> student

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
        1 * schoolRepository.get() >> [school]
        1 * teachingGroupRepository.getResourceByLink(new Link(verdi: 'link.to.TeachingGroup')) >> teachingGroup
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> studentRelation
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> student

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
        1 * schoolRepository.get() >> [school]
        1 * basisGroupRepository.getResourceByLink(new Link(verdi: 'link.to.BasisGroup')) >> basisGroup
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> studentRelation
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> student

        customerList.size() == 1
        customerList.get(0).name == '1TIA'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }

    def 'Given valid schoolId do'() {
        given:
        def school = fintObjectFactory.newSchool()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def result = groupService.getCustomersForSchoolWithVisIdKey('NO123456789')
        then:
        1 * schoolRepository.get() >> [school]
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> studentRelation
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> student
        noExceptionThrown()
        !result.isEmpty()
    }

    def "Given valid schoolId get only active students"() {
        given:
        def school = fintObjectFactory.newSchool()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def calender = Calendar.getInstance()
        calender.add(Calendar.DATE, -1)
        def periode = new Periode(start: calender.getTime(), slutt: calender.getTime())

        studentRelation.setGyldighetsperiode(periode)
        def student = fintObjectFactory.newStudent()

        when:
        def customerGroup = groupService.getCustomerGroupBySchool('NO123456789')

        then:
        1 * schoolRepository.get() >> [school]
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> studentRelation
        0 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> student

        customerGroup.name == 'HVS'
        customerGroup.customers.isEmpty()
    }

}
