package no.fint.betaling.service.groups

import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.fintdata.StudentRelationRepository
import no.fint.betaling.fintdata.StudentRepository
import no.fint.betaling.group.SchoolGroupService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.felles.kompleksedatatyper.Periode
import no.fint.model.resource.Link
import spock.lang.Specification

class SchoolGroupServiceSpec extends Specification {

    private FintObjectFactory fintObjectFactory
    private SchoolGroupService schoolGroupService
    private SchoolRepository schoolRepository = Mock()
    private StudentRelationRepository studentRelationRepository = Mock()
    private StudentRepository studentRepository = Mock()

    void setup() {
        schoolGroupService = new SchoolGroupService(schoolRepository, studentRepository, studentRelationRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Given valid schoolId get customer group from school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerGroup = schoolGroupService.getFromSchool('NO123456789')

        then:
        1 * schoolRepository.get() >> [school]
        1 * studentRelationRepository.getResourceByLink(_) >> Optional.of(studentRelation)
        1 * studentRepository.getResourceByLink(_) >> Optional.of(student)

        customerGroup.name == 'HVS'
        customerGroup.customers.get(0).id == '21i3v9'
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
        def customerGroup = schoolGroupService.getFromSchool('NO123456789')

        then:
        1 * schoolRepository.get() >> [school]
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> Optional.of(studentRelation)
        0 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> Optional.of(student)

        customerGroup.name == 'HVS'
        customerGroup.customers.isEmpty()
    }

}
