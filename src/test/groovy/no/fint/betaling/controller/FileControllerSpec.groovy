package no.fint.betaling.controller

import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.service.FileService
import no.fint.betaling.service.GroupService
import no.fint.betaling.util.CustomerFileGroup
import org.apache.poi.ss.usermodel.Sheet
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import spock.lang.Specification

@WebFluxTest(controllers = FileController.class)
class FileControllerSpec extends Specification {

    @Autowired
    private ApplicationContext applicationContext

    private WebTestClient webTestClient

    private FileController fileController

    @SpringBean
    private FileService fileService = Mock(FileService.class)

    @SpringBean
    private GroupService groupService = Mock(GroupService.class)

    void setup() {
        fileController = new FileController(fileService, groupService)
        webTestClient = WebTestClient.bindToController(fileController).build()
    }

    def "Get customers with correct file structure returns HttpStatus 200 with correct body"() {
        given:
        def result = new CustomerFileGroup(
                notFoundCustomers: ['123'],
                foundCustomers: new CustomerGroup(
                        customers: [new Customer()]
                )
        )

        when:
        def response = webTestClient
                .post()
                .uri('/api/file')
                .header('x-school-org-id', '')
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new byte[1]))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * fileService.getSheetFromBytes(_) >> Mock(Sheet)
        1 * fileService.extractCustomerFileGroupFromSheet(_, _) >> result
        1 * groupService.getCustomersForSchoolWithVisIdKey(_) >> ['hei': new Customer()]
        response
                .jsonPath('$.foundCustomers.customers.length()').isEqualTo(1)
        //.jsonPath() andExpect(jsonPathSize('$.foundCustomers.customers', 1))
                //.andExpect(jsonPathSize('$.notFoundCustomers', 1))
                .jsonPath('$.notFoundCustomers.length()').isEqualTo(1)
    }

}