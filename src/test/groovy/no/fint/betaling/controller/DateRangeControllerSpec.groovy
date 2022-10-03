package no.fint.betaling.controller

import no.fint.betaling.Application
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(controllers = DateRangeController.class)
@TestPropertySource(properties = "fint.betaling.date-range=7, 14, 30")
class DateRangeControllerSpec extends Specification {

    @Autowired
    private ApplicationContext applicationContext

    private WebTestClient webTestClient

    def "Get date ranges returns list"() {
        given:
        // Todo: this didn't work, so added the value as default to the field
        //def controller = new DateRangeController(dateRanges: ["7", "14", "30"] as String[])

        def controller = new DateRangeController()
        webTestClient = WebTestClient.bindToController(controller).build()

        when:
        def response = webTestClient
                .get()
                .uri('/api/date-range')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        response
                .jsonPath('$.length()').isEqualTo(3)
                .jsonPath('$[0]').isEqualTo("7")
    }
}
