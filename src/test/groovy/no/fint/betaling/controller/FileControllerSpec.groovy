package no.fint.betaling.controller

import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.service.FileService
import no.fint.betaling.service.GroupService
import no.fint.betaling.util.CustomerFileGroup
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

class FileControllerSpec extends Specification {
    private MockMvc mockMvc
    private FileController fileController
    private FileService fileService
    private GroupService groupService

    void setup() {
        fileService = Mock()
        groupService = Mock()
        fileController = new FileController(fileService: fileService, groupService: groupService)
        mockMvc = standaloneSetup(fileController)
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
        def response = mockMvc.perform(post('/api/file').header('x-school-org-id', '').content(new byte[1]))

        then:
        1 * fileService.getSheetFromBytes(_) >> Mock(Sheet)
        1 * fileService.extractCustomerFileGroupFromSheet(_, _) >> result
        1 * groupService.getCustomersForSchoolWithVisIdKey(_) >> ['hei':new Customer()]
        response
                .andExpect(status().isOk())
                .andExpect(jsonPathSize('$.foundCustomers.customers', 1))
                .andExpect(jsonPathSize('$.notFoundCustomers', 1))
    }

}