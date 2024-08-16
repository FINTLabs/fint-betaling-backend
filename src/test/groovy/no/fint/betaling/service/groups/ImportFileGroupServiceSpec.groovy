package no.fint.betaling.service.groups

import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.fintdata.StudentRelationRepository
import no.fint.betaling.fintdata.StudentRepository
import no.fint.betaling.group.ImportFileGroupService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

class ImportFileGroupServiceSpec extends Specification {

    private FintObjectFactory fintObjectFactory
    private ImportFileGroupService importFileGroupService
    private SchoolRepository schoolRepository = Mock()
    private StudentRelationRepository studentRelationRepository = Mock()
    private StudentRepository studentRepository = Mock()

    void setup() {
        importFileGroupService = new ImportFileGroupService(schoolRepository, studentRelationRepository, studentRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def 'Given valid schoolId do'() {
        given:
        def school = fintObjectFactory.newSchool()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def result = importFileGroupService.getCustomersForSchoolWithVisIdKey('NO123456789')
        then:
        1 * schoolRepository.get() >> [school]
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> Optional.of(studentRelation)
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> Optional.of(student)
        noExceptionThrown()
        !result.isEmpty()
    }
}
