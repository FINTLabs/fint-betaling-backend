package no.fint.betaling.controller

import no.fint.betaling.config.ApplicationProperties
import no.fint.betaling.model.Principal
import no.fint.betaling.service.PrincipalService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(controllers = PrincipalController.class)

class PrincipalControllerSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    private WebTestClient webTestClient

    private PrincipalController controller

    @SpringBean
    private PrincipalService principalService = Mock(PrincipalService.class)

    @SpringBean
    private ApplicationProperties applicationProperties = new ApplicationProperties()

    void setup() {
        applicationProperties.demo = true
        controller = new PrincipalController(principalService, applicationProperties)
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    def "Get employers given valid org id returns list"() {
        given:
//        def headers = new HttpHeaders()
//        headers.add('x-school-org-id', '12345');
//        headers.add('x-feide-upn', 'user@feide.no')

        when:
        def response = webTestClient
                .mutateWith(mockUser())
                .get()
                .uri('/api/principal')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        1 * principalService.getPrincipalByOrganisationId('12345', 'user@feide.no') >> new Principal(description: 'test')
        response.jsonPath('$.description').isEqualTo("test")
    }
}
