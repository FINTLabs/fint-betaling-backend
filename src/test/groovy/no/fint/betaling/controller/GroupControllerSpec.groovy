package no.fint.betaling.controller

import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.service.GroupService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class GroupControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private GroupController groupController
    private GroupService groupService

    void setup() {
        def customer = new Customer(name: 'Testesen')
        groupService = Mock(GroupService) {
            getCustomerGroupBySchool(_ as String, _ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [customer])]
            getCustomerGroupsByBasisGroupsAndSchool(_ as String, _ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [customer])]
            getCustomerGroupsByContactTeacherGroupsAndSchool(_ as String, _ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [customer])]
            getCustomerGroupsByTeachingGroupsAndSchool(_ as String, _ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [customer])]
        }
        groupController = new GroupController(groupService: groupService)
        mockMvc = standaloneSetup(groupController)
    }

    def "Get customers from school"() {
        when:
        def response = mockMvc.perform(get('/api/group/school'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$.name', 'testGroup'))
                .andExpect(jsonPathEquals('$.customers[0].name', 'Testesen'))
    }

    def "Get customer groups from basis groups"() {
        when:
        def response = mockMvc.perform(get('/api/group/basis-group'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].name', 'testGroup'))
                .andExpect(jsonPathEquals('$[0].customers[0].name', 'Testesen'))
    }

    def "Get customer groups from teaching groups"() {
        when:
        def response = mockMvc.perform(get('/api/group/teaching-group'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].name', 'testGroup'))
                .andExpect(jsonPathEquals('$[0].customers[0].name', 'Testesen'))
    }

    def "Get customer groups from contact teacher groups"() {
        when:
        def response = mockMvc.perform(get('/api/group/contact-teacher-group'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].name', 'testGroup'))
                .andExpect(jsonPathEquals('$[0].customers[0].name', 'Testesen'))
    }
}
