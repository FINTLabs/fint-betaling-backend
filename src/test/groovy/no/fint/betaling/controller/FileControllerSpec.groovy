package no.fint.betaling.controller

import no.fint.betaling.file.FileController
import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import no.fint.betaling.file.FileService
import no.fint.betaling.group.GroupService
import no.fint.betaling.common.util.CustomerFileGroup
import org.apache.poi.ss.usermodel.Sheet
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus
import spock.lang.Specification

class FileControllerSpec extends Specification {

    private FileController fileController

    @SpringBean
    FileService fileService = Mock()

    @SpringBean
    GroupService groupService = Mock()

    void setup() {
        fileController = new FileController(fileService, groupService)
    }

    def "Get normal result for customer file group"() {
        given:
        def fileGroup = new CustomerFileGroup(
                notFoundCustomers: ['123'],
                foundCustomers: new CustomerGroup(
                        customers: [new Customer()]
                )
        )

        when:
        def result = fileController.getCustomersOnFile('', new byte[1])

        then:
        1 * fileService.getSheetFromBytes(_) >> Mock(Sheet)
        1 * fileService.extractCustomerFileGroupFromSheet(_, _) >> fileGroup
        1 * groupService.getCustomersForSchoolWithVisIdKey(_) >> ['hei': new Customer()]

        result.statusCode == HttpStatus.OK
        result.getBody() == fileGroup
    }
}