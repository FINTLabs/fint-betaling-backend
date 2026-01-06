package no.fint.betaling.service.groups

import no.fint.betaling.fintdata.*
import no.fint.betaling.group.KlasseService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

class KlasseServiceSpec extends Specification {

    private FintObjectFactory fintObjectFactory
    private KlasseService klasseService
    private SchoolRepository schoolRepository = Mock()
    private KlasseRepository klasseRepository = Mock()
    private StudentRelationRepository studentRelationRepository = Mock()
    private StudentRepository studentRepository = Mock()
    private klassemedlemskapRepository klassemedlemskapRepository = Mock()

    void setup() {
        klasseService = new KlasseService(schoolRepository, klasseRepository, klassemedlemskapRepository, studentRepository, studentRelationRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Given valid schoolId get customer groups from klasse at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def klasse = fintObjectFactory.newKlasse()
        def klassemedlemskap = fintObjectFactory.newKlassemedlemskap()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerList = klasseService.getFromKlasse('NO123456789')

        then:
        1 * schoolRepository.get() >> [school]
        1 * klasseRepository.getResourceByLink(new Link(verdi: 'link.to.Klasse')) >> Optional.of(klasse)
        1 * klassemedlemskapRepository.getResourceByLink(new Link(verdi: 'link.to.Klassemedlemskap')) >> Optional.of(klassemedlemskap)
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> Optional.of(studentRelation)
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> Optional.of(student)

        customerList.size() == 1
        customerList.get(0).name == '1TIA'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }
}
