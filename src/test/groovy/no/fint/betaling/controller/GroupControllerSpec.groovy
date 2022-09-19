package no.fint.betaling.controller

import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.service.GroupService
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

class GroupControllerSpec extends Specification {
    private MockMvc mockMvc
    private GroupController groupController
    private GroupService groupService

    void setup() {
        groupService = Mock(GroupService)
        groupController = new GroupController(groupService)
        mockMvc = standaloneSetup(groupController)
    }

    def "Get customers from school"() {
        when:
        def response = mockMvc.perform(get('/api/group/school').header('x-school-org-id', 'DUMMY'))

        then:
        1 * groupService.getCustomerGroupBySchool(_ as String) >> new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.name', 'testGroup'))
                .andExpect(jsonPathEquals('$.customers[0].name', 'Testesen'))
    }

    def "Get customer groups from basis groups"() {
        when:
        def response = mockMvc.perform(get('/api/group/basis-group').header('x-school-org-id', 'DUMMY'))

        then:
        1 * groupService.getCustomerGroupsByBasisGroupsAndSchool(_ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].name', 'testGroup'))
                .andExpect(jsonPathEquals('$[0].customers[0].name', 'Testesen'))
    }

    def "Get customer groups from teaching groups"() {
        when:
        def response = mockMvc.perform(get('/api/group/teaching-group').header('x-school-org-id', 'DUMMY'))

        then:
        1 * groupService.getCustomerGroupsByTeachingGroupsAndSchool(_ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].name', 'testGroup'))
                .andExpect(jsonPathEquals('$[0].customers[0].name', 'Testesen'))
    }

    def "Get customer groups from contact teacher groups"() {
        when:
        def response = mockMvc.perform(get('/api/group/contact-teacher-group').header('x-school-org-id', 'DUMMY'))

        then:
        1 * groupService.getCustomerGroupsByContactTeacherGroupsAndSchool(_ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].name', 'testGroup'))
                .andExpect(jsonPathEquals('$[0].customers[0].name', 'Testesen'))
    }
}