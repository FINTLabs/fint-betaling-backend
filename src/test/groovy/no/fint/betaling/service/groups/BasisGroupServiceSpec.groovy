package no.fint.betaling.service.groups

import no.fint.betaling.fintdata.*
import no.fint.betaling.group.BasisGroupService
import no.fint.betaling.util.FintObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

class BasisGroupServiceSpec extends Specification {

    private FintObjectFactory fintObjectFactory
    private BasisGroupService basisGroupService
    private SchoolRepository schoolRepository = Mock()
    private BasisGroupRepository basisGroupRepository = Mock()
    private StudentRelationRepository studentRelationRepository = Mock()
    private StudentRepository studentRepository = Mock()
    private BasisGroupMembershipRepository basisGroupMembershipRepository = Mock()

    void setup() {
        basisGroupService = new BasisGroupService(schoolRepository, basisGroupRepository, basisGroupMembershipRepository, studentRepository, studentRelationRepository)
        fintObjectFactory = new FintObjectFactory()
    }

    def "Given valid schoolId get customer groups from basis groups at school"() {
        given:
        def school = fintObjectFactory.newSchool()
        def basisGroup = fintObjectFactory.newBasisGroup()
        def basisGroupMembership = fintObjectFactory.newBasisGroupMembership()
        def studentRelation = fintObjectFactory.newStudentRelation()
        def student = fintObjectFactory.newStudent()

        when:
        def customerList = basisGroupService.getFromBasisGroups('NO123456789')

        then:
        1 * schoolRepository.get() >> [school]
        1 * basisGroupRepository.getResourceByLink(new Link(verdi: 'link.to.BasisGroup')) >> Optional.of(basisGroup)
        1 * basisGroupMembershipRepository.getResourceByLink(new Link(verdi: 'link.to.BasisGroupMembership')) >> Optional.of(basisGroupMembership)
        1 * studentRelationRepository.getResourceByLink(new Link(verdi: 'link.to.StudentRelation')) >> Optional.of(studentRelation)
        1 * studentRepository.getResourceByLink(new Link(verdi: 'link.to.Student')) >> Optional.of(student)

        customerList.size() == 1
        customerList.get(0).name == '1TIA'
        customerList.get(0).customers.get(0).id == '21i3v9'
    }
}
