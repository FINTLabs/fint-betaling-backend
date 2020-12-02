package no.fint.betaling.service


import no.fint.betaling.exception.InsufficientDataException
import no.fint.betaling.exception.NoVISIDColumnException
import no.fint.betaling.model.Customer
import no.fint.betaling.util.FintObjectFactory
import org.apache.commons.io.FileUtils
import org.springframework.web.HttpMediaTypeNotAcceptableException
import spock.lang.Specification

class FileServiceSpec extends Specification {
    private FileService fileService
    private FintObjectFactory fintObjectFactory

    void setup() {
        fileService = new FileService(
                fileTypes:["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/vnd.ms-excel"],
                visId: "VIS-ID"
        )
        fintObjectFactory = new FintObjectFactory()

    }

    def "Given valid excel list, response contains found and not found customers"(){
        given:
        def resource = new File(getClass().getResource('/dummy_excel_customer_list.xlsx').toURI())
        def array = FileUtils.readFileToByteArray(resource)
        def customers = ['12345': new Customer(id: 'FOUNDIT')]

        when:
        def sheet = fileService.getSheetFromBytes(array)

        then:
        noExceptionThrown()

        when:
        def response = fileService.extractCustomerFileGroupFromSheet(sheet, customers)

        then:
        response.foundCustomers.customers.every { it.id == 'FOUNDIT' }
        response.notFoundCustomers.size() == 5
        response.hasCustomers()
    }

    def 'Given file with file type not matching @Value ${fint.betaling.dnd.VIS-ID}, UnsupportedMediaException should be thrown'() {
        given:
        def resource = new File(getClass().getResource('/dummy_fakturagrunnlag.json').toURI())
        def array = FileUtils.readFileToByteArray(resource)

        when:
        fileService.getSheetFromBytes(array)

        then:
        thrown(HttpMediaTypeNotAcceptableException)
    }

    def 'Given file with no "VIS-ID" column, NoVISIDColumnException should be thrown'() {
        given:
        def resource = new File(getClass().getResource('/dummy_excel_missing_vis_id_column.xlsx').toURI())
        def array = FileUtils.readFileToByteArray(resource)
        def customers = ['12345': new Customer(id: 'FOUNDIT')]

        when:
        def sheet = fileService.getSheetFromBytes(array)
        fileService.extractCustomerFileGroupFromSheet(sheet, customers)

        then:
        thrown(NoVISIDColumnException)
    }
    def 'Given empty list, InsufficientDataException should be thrown'() {
        given:
        def resource = new File(getClass().getResource('/dummy_excel_empty_list.xlsx').toURI())
        def array = FileUtils.readFileToByteArray(resource)
        def customers = ['12345': new Customer(id: 'FOUNDIT')]

        when:
        def sheet = fileService.getSheetFromBytes(array)
        fileService.extractCustomerFileGroupFromSheet(sheet, customers)

        then:
        thrown(InsufficientDataException)
    }
}
