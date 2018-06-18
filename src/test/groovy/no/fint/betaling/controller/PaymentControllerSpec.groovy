package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Fakturagrunnlag
import no.fint.betaling.model.Kunde
import no.fint.betaling.model.Payment
import no.fint.betaling.service.PaymentService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

class PaymentControllerSpec extends MockMvcSpecification {//TODO: send orgId i HTTP header
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
        def response = mockMvc.perform(get('/api/payment'))

        then:
        1 * paymentService.getAllPayments('test.no') >> [createPayment('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn.etternavn', 'Testesen'))
    }

    def "Set payment given payment returns Betaling"() {
        given:
        def kunde = new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        def fakturagrunnlag = new Fakturagrunnlag(total: 1000, systemId: new Identifikator(identifikatorverdi: 'test'))

        def objectMapper = new ObjectMapper()
        def jsonPayment = objectMapper.writeValueAsString(new Payment())

        when:
        def response = mockMvc.perform(post('/api/payment/save').content(jsonPayment).contentType(MediaType.APPLICATION_JSON))

        then:
        1 * paymentService.setPayment('test.no', _, _) >> new Betaling(kunde: kunde, fakturagrunnlag: fakturagrunnlag)
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.kunde.navn.etternavn', 'Testesen'))
                //TODO: test på total-verdi i fakturagrunnlag
    }

    def "Get payment by name given lastname returns list of payments with matching lastname"() {
        when:
        def response = mockMvc.perform(get('/api/payment/navn').param('etternavn', 'Testesen'))

        then:
        1 * paymentService.getPaymentsByLastname(_, 'Testesen') >> [createPayment('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn.etternavn', 'Testesen'))
    }

    def "Get payment by ordernumber given valid ordernumber returns list of payments with matching ordernumber"() {
        when:
        def response = mockMvc.perform(get('/api/payment/ordrenummer').param('ordrenummer', 'validOrdernumber'))

        then:
        1 * paymentService.getPaymentsByOrdernumber(_, 'validOrdernumber') >> [createPayment('validOrdernumber', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].ordrenummer', 'validOrdernumber'))
    }

    private static Betaling createPayment(String ordernumber, String lastname) {
        return new Betaling(kunde: new Kunde(navn: new Personnavn(etternavn: lastname)), ordrenummer: ordernumber)
    }
}
