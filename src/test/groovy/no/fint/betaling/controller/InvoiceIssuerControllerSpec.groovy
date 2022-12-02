package no.fint.betaling.controller

import no.fint.betaling.config.ApplicationProperties
import no.fint.betaling.model.Principal
import no.fint.betaling.service.InvoiceIssuerService
import spock.lang.Ignore
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(controllers = InvoiceIssuerController.class)

class InvoiceIssuerControllerSpec extends Specification {

    @Autowired
    private ApplicationContext applicationContext

    private WebTestClient webTestClient

    private InvoiceIssuerController controller

    @SpringBean
    private InvoiceIssuerService principalService = Mock(InvoiceIssuerService.class)

    @SpringBean
    private ApplicationProperties applicationProperties = new ApplicationProperties()

    void setup() {
        applicationProperties.demo = true
        controller = new InvoiceIssuerController(principalService, applicationProperties)
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    @Ignore
    def "Get employers given valid org id returns list"() {
        given:
//        def headers = new HttpHeaders()
//        headers.add('x-school-org-id', '12345');
//        headers.add('x-feide-upn', 'user@feide.no')

        when:
        def response = webTestClient
                //.mutateWith(mockUser())
                .get()
                .uri('/api/principal')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * principalService.getInvoiceIssuer('12345', 'user@feide.no') >> new Principal(description: 'test')
        response.jsonPath('$.description').isEqualTo("test")
    }
}
