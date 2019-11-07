package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Kunde
import no.fint.betaling.model.Payment

import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.hamcrest.CoreMatchers
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

class ClaimControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private ClaimController paymentController
    private PaymentRepository paymentRepository

    void setup() {
        paymentRepository = Mock(PaymentRepository)
        paymentController = new ClaimController(paymentRepository: paymentRepository)
        mockMvc = standaloneSetup(paymentController)
    }

    def "Get all payments"() {
        when:
        def response = mockMvc.perform(get('/api/payment').header('x-org-id','test.no'))

        then:
        1 * paymentRepository.getAllPayments('test.no') >> [createPayment(123, 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn.etternavn', 'Testesen'))
    }

    def "Set payment given valid payment returns status ok"() {
        given:
        def objectMapper = new ObjectMapper()
        def jsonPayment = objectMapper.writeValueAsString(new Payment())

        when:
        def response = mockMvc.perform(post('/api/payment').content(jsonPayment).contentType(MediaType.APPLICATION_JSON).header('x-org-id', 'test.no'))

        then:
        1 * paymentRepository.setPayment('test.no', _ as Payment)
        response.andExpect(status().is(201))
    }

    def "Get payment by name given lastname returns list of payments with matching lastname"() {
        when:
        def response = mockMvc.perform(get('/api/payment/navn/{etternavn}', 'Testesen'))

        then:
        1 * paymentRepository.getPaymentsByCustomerName(_, 'Testesen') >> [createPayment(123, 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn.etternavn', 'Testesen'))
    }

    def "Get payment by orderNumber given valid orderNumber returns list of payments with matching orderNumber"() {
        when:
        def response = mockMvc.perform(get('/api/payment/ordrenummer/{ordrenummer}', '123'))

        then:
        1 * paymentRepository.getPaymentsByOrdernumber(_, '123') >> [createPayment(123, 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPath('$[0].ordrenummer', CoreMatchers.equalTo(123)))
    }

    private static Betaling createPayment(long orderNumber, String lastname) {
        return new Betaling(kunde: new Kunde(navn: new Personnavn(etternavn: lastname)), ordrenummer: orderNumber)
    }
}
