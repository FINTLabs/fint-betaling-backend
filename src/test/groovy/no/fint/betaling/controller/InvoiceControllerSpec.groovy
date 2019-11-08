package no.fint.betaling.controller

import no.fint.betaling.service.ClaimService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class InvoiceControllerSpec extends MockMvcSpecification {
    private ClaimService invoiceRepository
    private InvoiceController controller
    private MockMvc mockMvc

    void setup() {
        invoiceRepository = Mock(ClaimService)
        controller = new InvoiceController(claimService: invoiceRepository)
        mockMvc = standaloneSetup(controller)
    }

    def "Send invoices given valid org id sends invoices"() {
        when:
        def response = mockMvc.perform(get('/api/invoice/send').header('x-org-id', 'valid.org'))

        then:
        1 * invoiceRepository.sendClaims('valid.org')
        response.andExpect(status().isNoContent())
    }

    def "Update invoices given valid org id updates invoices"() {
        when:
        def response = mockMvc.perform(get('/api/invoice/update').header('x-org-id', 'valid.org'))

        then:
        1 * invoiceRepository.updateClaimStatus('valid.org')
        response.andExpect(status().isNoContent())
    }
}
