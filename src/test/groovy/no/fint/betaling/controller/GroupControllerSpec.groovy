package no.fint.betaling.controller

import no.fint.betaling.group.GroupController
import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.group.GroupService
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus
import spock.lang.Specification

class GroupControllerSpec extends Specification {

    GroupController groupController

    CustomerGroup customerGroup

    @SpringBean
    private GroupService groupService = Mock(GroupService.class)

    void setup() {
        groupController = new GroupController(groupService)
        customerGroup = new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])
    }

    def "Get customers from school"() {

        when:
        def response = groupController.getCustomerGroupBySchool('DUMMY')

        then:
        1 * groupService.getCustomerGroupBySchool('DUMMY') >> customerGroup
        response.statusCode == HttpStatus.OK
        response.getBody() == customerGroup
    }

    def "Get customer groups from basis groups"() {

        when:
        def response = groupController.getCustomerGroupsByBasisGroupsAndSchool('DUMMY')

        then:
        1 * groupService.getCustomerGroupsByBasisGroupsAndSchool('DUMMY') >> [customerGroup]
        response.statusCode == HttpStatus.OK
        response.getBody() == [customerGroup]
    }

    def "Get customer groups from teaching groups"() {

        when:
        def response = groupController.getCustomerGroupsByTeachingGroupsAndSchool('DUMMY')

        then:
        1 * groupService.getCustomerGroupsByTeachingGroupsAndSchool('DUMMY') >> [customerGroup]
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == [customerGroup]
    }

    def "Get customer groups from contact teacher groups"() {

        when:
        def response = groupController.getCustomerGroupsByContactTeacherGroupsAndSchool('DUMMY')

        then:
        1 * groupService.getCustomerGroupsByContactTeacherGroupsAndSchool('DUMMY') >> [customerGroup]
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == [customerGroup]
    }
}