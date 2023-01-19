package no.fint.betaling.controller


import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.service.ClaimService
import no.fint.betaling.service.ScheduleService
import org.hamcrest.CoreMatchers
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import spock.lang.Ignore
import spock.lang.Specification

@WebFluxTest(controllers = ClaimController.class)
class ClaimControllerSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    private WebTestClient webTestClient

    private ClaimController controller

    @SpringBean
    private ClaimService claimService = Mock(ClaimService.class)

    @SpringBean
    private ScheduleService scheduleService = Mock(ScheduleService.class)


    void setup() {
        controller = new ClaimController(claimService, scheduleService)
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    def "Get all payments"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/claim')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * claimService.getClaims(_,_,_) >> [createClaim('123', 'Testesen')]
        response.jsonPath('$[0].customer.name').isEqualTo("Testesen")
    }

    def "Set payment given valid payment returns status ok"() {
        when:
        def response = webTestClient
                .post()
                .uri('/api/claim')
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new Order()))
                .exchange()
                .expectStatus()

        then:
        1 * claimService.storeClaims(_ as Order)
        response.isEqualTo(HttpStatus.CREATED)
    }

    def "Get payment by name given lastname returns list of payments with matching lastname"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/claim/name/{name}', 'Testesen')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * claimService.getClaimsByCustomerName('Testesen') >> [createClaim('123', 'Testesen')]
        response.jsonPath('$[0].customer.name').isEqualTo("Testesen")
    }

    def "Get payment by orderNumber given valid orderNumber returns list of payments with matching orderNumber"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/claim/order-number/{order-number}', '123')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * claimService.getClaimsByOrderNumber('123') >> [createClaim('123', 'Testesen')]
        response
                .jsonPath('$.length()').isEqualTo(1)
                .jsonPath('$[0].orderNumber', CoreMatchers.equalTo('123'))
    }

    def "Send invoices given valid org id sends invoices"() {
        when:
        def response = webTestClient
                .post()
                .uri('/api/claim/send')
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(["123", "654"]))
                .exchange()
                .expectStatus()
                .isCreated()

        then:
        1 * claimService.sendClaims(["123", "654"] as List)
    }

    def "Get a total count of invoices based on status and on number of days since created"() {
        when:
        def response = webTestClient
                .get()
                .uri('/api/claim/count/status/{status}?days={days}', 'ERROR', '14')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * claimService.countClaimsByStatus([ClaimStatus.ERROR] as ClaimStatus[], '14') >> 78
        response.jsonPath('$').isEqualTo(78)
    }

    @Ignore
    def "Update invoices given valid org id updates invoices"() {
        when:
        def response = mockMvc.perform(get('/api/claim/update'))

        then:
        1 * claimService.updateClaimStatus()
        response.andExpect(status().isNoContent())
    }

    private static Claim createClaim(String orderNumber, String lastName) {
        return new Claim(customer: new Customer(name: lastName), orderNumber: orderNumber)
    }
}
