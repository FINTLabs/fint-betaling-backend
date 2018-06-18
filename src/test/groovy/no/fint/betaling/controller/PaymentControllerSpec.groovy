package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Fakturagrunnlag
import no.fint.betaling.model.Kunde
import no.fint.betaling.model.Payment
import no.fint.betaling.service.MongoService
import no.fint.betaling.service.PaymentService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

class PaymentControllerSpec extends MockMvcSpecification {//TODO: finn ut hvordan man legger til HTTP headere
    private MockMvc mockMvc
    private PaymentController paymentController
    private PaymentService paymentService
    private MongoService mongoService

    void setup() {
        paymentService = Mock(PaymentService)
        mongoService = Mock(MongoService)
        paymentController = new PaymentController(paymentService: paymentService, mongoService: mongoService)
        mockMvc = standaloneSetup(paymentController)
    }

    def "Get all payments"() {
        when:
        def response = mockMvc.perform(get('/api/betaling'))

        then:
        1 * paymentService.getAllPayments('test.no') >> [createPayment('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn.etternavn', 'Testesen'))
    }

    def "Set payment given payment returns Betaling"() {//TODO: skriv testen ferdig
        given:
        def kunde = new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        def fakturagrunnlag = new Fakturagrunnlag(total: 1000, systemId: new Identifikator(identifikatorverdi: 'test'))

        def objectMapper = new ObjectMapper()
        def jsonPayment = objectMapper.writeValueAsString(new Payment())

        when:
        def response = mockMvc.perform(post('/api/betaling/save').content(jsonPayment).contentType(MediaType.APPLICATION_JSON))

        then:
        1 * mongoService.saveFakturagrunnlag('test.no', _, _) >> new Betaling(kunde: kunde, fakturagrunnlag: fakturagrunnlag)
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.kunde.navn.etternavn', 'Testesen'))
    }

    def "Get payment by name given lastname returns list of payments with matching lastname"() {
        when:
        def response = mockMvc.perform(get('/api/betaling/navn').param('etternavn', 'Testesen'))

        then:
        1 * paymentService.getPaymentsByLastname(_, 'Testesen') >> [createPayment('123', 'Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kunde.navn.etternavn', 'Testesen'))
    }

    def "Get payment by ordernumber given valid ordernumber returns list of payments with matching ordernumber"(){
        when:
        def response = mockMvc.perform(get('/api/betaling/ordrenummer').param('ordrenummer','validOrdernumber'))

        then:
        1 * paymentService.getPaymentsByOrdernumber(_,'validOrdernumber') >> [createPayment('validOrdernumber','Testesen')]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$',1))
                .andExpect(jsonPathEquals('$[0].ordrenummer', 'validOrdernumber'))
    }

    private static Betaling createPayment(String ordernumber, String lastname) {
        return new Betaling(kunde: new Kunde(navn: new Personnavn(etternavn: lastname)), ordrenummer: ordernumber)
    }
}
