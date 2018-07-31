package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Kunde
import no.fint.betaling.model.Payment
import no.fint.betaling.service.PaymentService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

class PaymentControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private PaymentController paymentController
    private PaymentService paymentService

    void setup() {
        paymentService = Mock(PaymentService)
        paymentController = new PaymentController(paymentService: paymentService)
        mockMvc = standaloneSetup(paymentController)
    }

    def "Get all payments"() {
        when:
        def response = mockMvc.perform(get('/api/payment').header('x-org-id','test.no'))

        then:
        1 * paymentService.getAllPayments('test.no') >> [createPayment('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn', 'Testesen'))
    }

    def "Set payment given valid payment returns status ok"() {
        given:
        def objectMapper = new ObjectMapper()
        def jsonPayment = objectMapper.writeValueAsString(new Payment())

        when:
        def response = mockMvc.perform(post('/api/payment').content(jsonPayment).contentType(MediaType.APPLICATION_JSON).header('x-org-id', 'test.no'))

        then:
        1 * paymentService.setPayment('test.no', _ as Payment)
        response.andExpect(status().is(201))
    }

    def "Get payment by name given lastname returns list of payments with matching lastname"() {
        when:
        def response = mockMvc.perform(get('/api/payment/navn/{navn}', 'Testesen'))

        then:
        1 * paymentService.getPaymentsByCustomerName(_, 'Testesen') >> [createPayment('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn', 'Testesen'))
    }

    def "Get payment by orderNumber given valid orderNumber returns list of payments with matching orderNumber"() {
        when:
        def response = mockMvc.perform(get('/api/payment/ordrenummer/{ordrenummer}', 'validOrderNumber'))

        then:
        1 * paymentService.getPaymentsByOrdernumber(_, 'validOrderNumber') >> [createPayment('validOrderNumber', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].ordrenummer', 'validOrderNumber'))
    }

    private static Betaling createPayment(String orderNumber, String lastname) {
        return new Betaling(kunde: new Kunde(navn: lastname), ordrenummer: orderNumber)
    }
}
