package no.fint.betaling.controller

import no.fint.betaling.model.Taxcode
import no.fint.betaling.repository.TaxcodeRepository
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(TaxcodeController.class)
class TaxcodeControllerSpec extends Specification {

    // private MockMvc mockMvc
    private TaxcodeController controller

    @SpringBean
    private TaxcodeRepository repository = Mock()

    @Autowired
    private WebTestClient webTestClient

    void setup() {
        //repository = Mock()
        controller = new TaxcodeController(repository)
        // mockMvc = standaloneSetup(controller)
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    def "Get mva codes given valid org id returns list -2"() {
        given:
        Taxcode taxcode = new Taxcode(code: '25%', description: 'Høy rate', rate: 0.25)
        //TaxcodeRepository repository = Mock()
        //repository.getTaxcodes() >> [taxcode]
        repository.getTaxcodes() >> [taxcode]

        when:
        def response = webTestClient
                .get()
                .uri('/api/mva-code')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        // String s = new String(response, StandardCharsets.UTF_8);
        then:
        1 * repository.getTaxcodes()

        // StepVerifier.create

        response
                .isEmpty()
                // .jsonPath('$[0].code').isEqualTo("25%")
    }

    def "Repository and response"() {
        given:
        Taxcode taxcode = new Taxcode(code: '25%', description: 'Høy rate', rate: 0.25)
        //TaxcodeRepository repository = Mock()
        repository.getTaxcodes() >> [taxcode]

        when:
        controller = new TaxcodeController(repository)
        controller.getMvaCodes()

        then:
        1 * repository.getTaxcodes() >> [taxcode]

    }


//    def "Get mva codes given valid org id returns list"() {
//        when:
//        def response = mockMvc.perform(get('/api/mva-code'))
//        webTestClient
//                .get()
//                .uri('/api/mva-code')
//                .exchange()
//                .expectStatus()
//                .isOk()
//
//
//        then:
//        1 * repository.getTaxcodes() >> [new Taxcode(code: '25%', description: 'Høy rate', rate: 0.25)]
//
//        response.andExpect(jsonPathSize('$', 1))
//                .andExpect(jsonPathEquals('$[0].code', '25%'))
//    }

}
