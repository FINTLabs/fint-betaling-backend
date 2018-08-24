package no.fint.betaling.controller

import no.fint.betaling.service.InvoiceService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class InvoiceControllerSpec extends MockMvcSpecification {
    private InvoiceService invoiceService
    private InvoiceController controller
    private MockMvc mockMvc

    void setup() {
        invoiceService = Mock(InvoiceService)
        controller = new InvoiceController(invoiceService: invoiceService)
        mockMvc = standaloneSetup(controller)
    }

    def "Send invoices given valid org id sends invoices"() {
        when:
        def response = mockMvc.perform(get('/api/invoice/send').header('x-org-id', 'valid.org'))

        then:
        1 * invoiceService.sendInvoices('valid.org')
        response.andExpect(status().isNoContent())
    }

    def "Update invoices given valid org id updates invoices"() {
        when:
        def response = mockMvc.perform(get('/api/invoice/update').header('x-org-id', 'valid.org'))

        then:
        1 * invoiceService.updateInvoiceStatus('valid.org')
        response.andExpect(status().isNoContent())
    }
}
