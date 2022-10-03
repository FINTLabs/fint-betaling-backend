package no.fint.betaling.controller

import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.service.GroupService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(controllers = GroupController.class)
class GroupControllerSpec extends Specification {

    @Autowired
    private ApplicationContext applicationContext

    private WebTestClient webTestClient

    private GroupController groupController

    @SpringBean
    private GroupService groupService = Mock(GroupService.class)

    void setup() {
        groupController = new GroupController(groupService)
        webTestClient = WebTestClient.bindToController(groupController).build()
    }

    def "Get customers from school"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/group/school')
                .header('x-school-org-id', 'DUMMY')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * groupService.getCustomerGroupBySchool(_ as String) >> new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])
        response
                .jsonPath('$.name').isEqualTo('testGroup')
                .jsonPath('$.customers[0].name').isEqualTo('Testesen')
    }

    def "Get customer groups from basis groups"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/group/basis-group')
                .header('x-school-org-id', 'DUMMY')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * groupService.getCustomerGroupsByBasisGroupsAndSchool(_ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])]
        response
                .jsonPath('$.length()').isEqualTo(1)
                .jsonPath('$[0].name').isEqualTo('testGroup')
                .jsonPath('$[0].customers[0].name').isEqualTo('Testesen')
    }

    def "Get customer groups from teaching groups"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/group/teaching-group')
                .header('x-school-org-id', 'DUMMY')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * groupService.getCustomerGroupsByTeachingGroupsAndSchool(_ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])]
        response
                .jsonPath('$.length()').isEqualTo(1)
                .jsonPath('$[0].name').isEqualTo('testGroup')
                .jsonPath('$[0].customers[0].name').isEqualTo('Testesen')
    }

    def "Get customer groups from contact teacher groups"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/group/contact-teacher-group')
                .header('x-school-org-id', 'DUMMY')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * groupService.getCustomerGroupsByContactTeacherGroupsAndSchool(_ as String) >> [new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])]
        response
                .jsonPath('$.length()').isEqualTo(1)
                .jsonPath('$[0].name').isEqualTo('testGroup')
                .jsonPath('$[0].customers[0].name').isEqualTo('Testesen')
    }
}