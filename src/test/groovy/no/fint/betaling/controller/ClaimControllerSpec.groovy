package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.model.Claim
import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.service.ClaimService
import no.fint.test.utils.MockMvcSpecification
import org.hamcrest.CoreMatchers
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

class ClaimControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private ClaimController claimController
    private ClaimService claimService

    void setup() {
        claimService = Mock(ClaimService)
        claimController = new ClaimController(claimService: claimService)
        mockMvc = standaloneSetup(claimController)
    }

    def "Get all payments"() {
        when:
        def response = mockMvc.perform(get('/api/claim').header('x-org-id','test.no'))

        then:
        1 * claimService.getAllClaims('test.no') >> [createClaim('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].customer.name', 'Testesen'))
    }

    def "Set payment given valid payment returns status ok"() {
        given:
        def objectMapper = new ObjectMapper()
        def jsonOrder = objectMapper.writeValueAsString(new Order())

        when:
        def response = mockMvc.perform(post('/api/claim').content(jsonOrder).contentType(MediaType.APPLICATION_JSON).header('x-org-id', 'test.no'))

        then:
        1 * claimService.setClaim('test.no', _ as Order)
        response.andExpect(status().is(201))
    }

    def "Get payment by name given lastname returns list of payments with matching lastname"() {
        when:
        def response = mockMvc.perform(get('/api/claim/name/{name}', 'Testesen'))

        then:
        1 * claimService.getClaimsByCustomerName(_, 'Testesen') >> [createClaim('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].customer.name', 'Testesen'))
    }

    def "Get payment by orderNumber given valid orderNumber returns list of payments with matching orderNumber"() {
        when:
        def response = mockMvc.perform(get('/api/claim/order-number/{order-number}', '123'))

        then:
        1 * claimService.getClaimsByOrderNumber(_, '123') >> [createClaim('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPath('$[0].orderNumber', CoreMatchers.equalTo('123')))
    }

    def "Send invoices given valid org id sends invoices"() {
        given:
        def objectMapper = new ObjectMapper()
        def jsonOrderNumbers = objectMapper.writeValueAsString(["123", "123"])

        when:
        def response = mockMvc.perform(post('/api/claim/send').content(jsonOrderNumbers).contentType(MediaType.APPLICATION_JSON).header('x-org-id', 'valid.org'))

        then:
        1 * claimService.sendClaims('valid.org', _ as List)
        response.andExpect(status().is(201))
    }

    def "Update invoices given valid org id updates invoices"() {
        when:
        def response = mockMvc.perform(get('/api/claim/update').header('x-org-id', 'valid.org'))

        then:
        1 * claimService.updateClaimStatus('valid.org')
        response.andExpect(status().isNoContent())
    }

    private static Claim createClaim(String orderNumber, String lastName) {
        return new Claim(customer: new Customer(name: lastName), orderNumber: orderNumber)
    }
}
