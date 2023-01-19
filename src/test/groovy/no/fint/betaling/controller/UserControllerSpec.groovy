package no.fint.betaling.controller

import no.fint.betaling.config.ApplicationProperties
import no.fint.betaling.model.User
import no.fint.betaling.service.UserCacheService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Ignore
import spock.lang.Specification

@WebFluxTest(controllers = UserController.class)
class UserControllerSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    private WebTestClient webTestClient

    private UserController controller

    @SpringBean
    private UserCacheService userCacheService = Mock(UserCacheService.class)

    @SpringBean
    private ApplicationProperties applicationProperties = new ApplicationProperties()

    void setup() {
        applicationProperties.setDemo(true)
        controller = new UserController(applicationProperties, userCacheService)
        webTestClient = WebTestClient.bindToController(controller).build()
    }

    @Ignore("Need to figureout how to use JWT in this test")
    def "Get me returns user"() {
        given:
        User user = new User()
        user.setName("Testesen")

        when:
        def response = webTestClient
                .get()
                .uri('/api/me')
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()

        then:
        //1 * applicationProperties.getDemo() >> true
        1 * userCacheService.getUser(_,_) >> user
        response.jsonPath('$.name').isEqualTo("Testesen")
    }
}