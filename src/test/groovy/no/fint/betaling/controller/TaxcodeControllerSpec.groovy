package no.fint.betaling.controller

import no.fint.betaling.model.Taxcode
import no.fint.betaling.repository.TaxcodeRepository
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(controllers = TaxcodeController.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaxcodeControllerSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    private WebTestClient webTestClient

    private TaxcodeController controller

    @SpringBean
    private TaxcodeRepository repository = Mock(TaxcodeRepository.class)

    void setup() {
        controller = new TaxcodeController(repository)
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    def "Get mva codes given valid org id returns list -2"() {
        given:
        Taxcode taxcode = new Taxcode(code: '25%', description: 'Høy rate', rate: 0.25)

        when:
        def response = webTestClient
                .get()
                .uri('/api/mva-code')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * repository.getTaxcodes() >> [taxcode]
        response.jsonPath('$[0].code').isEqualTo("25%")
    }
}
