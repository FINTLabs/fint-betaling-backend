package no.fint.betaling.controller

import no.fint.betaling.group.*
import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import org.springframework.http.HttpStatus
import spock.lang.Specification

class GroupControllerSpec extends Specification {

    GroupController groupController

    CustomerGroup customerGroup

    private SchoolGroupService schoolGroupService = Mock()

    private BasisGroupService basisGroupService = Mock()

    private TeachingGroupService teachingGroupService = Mock()

    private ContactTeacherGroupService contactTeacherGroupService = Mock()

    void setup() {
        groupController = new GroupController(schoolGroupService, basisGroupService, teachingGroupService, contactTeacherGroupService)
        customerGroup = new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])
    }

    def "Get customers from school"() {

        when:
        def response = groupController.getFromSchool('DUMMY')

        then:
        1 * schoolGroupService.getFromSchool('DUMMY') >> customerGroup
        response.statusCode == HttpStatus.OK
        response.getBody() == customerGroup
    }

    def "Get customer groups from basis groups"() {

        when:
        def response = groupController.getFromBasisGroups('DUMMY')

        then:
        1 * basisGroupService.getFromBasisGroups('DUMMY') >> [customerGroup]
        response.statusCode == HttpStatus.OK
        response.getBody() == [customerGroup]
    }

    def "Get customer groups from teaching groups"() {

        when:
        def response = groupController.getFromTeachingGroups('DUMMY')

        then:
        1 * teachingGroupService.getFromTeachingGroups('DUMMY') >> [customerGroup]
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == [customerGroup]
    }

    def "Get customer groups from contact teacher groups"() {

        when:
        def response = groupController.getFromContactTeacherGroups('DUMMY')

        then:
        1 * contactTeacherGroupService.getFromContactTeacherGroups('DUMMY') >> [customerGroup]
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == [customerGroup]
    }
}