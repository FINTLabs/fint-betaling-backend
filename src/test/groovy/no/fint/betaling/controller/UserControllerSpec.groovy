package no.fint.betaling.controller

import no.fint.betaling.common.config.ApplicationProperties
import no.fint.betaling.common.exception.EmployeeIdException
import no.fint.betaling.model.User
import no.fint.betaling.user.UserCacheService
import no.fint.betaling.user.UserController
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Specification
import reactor.core.publisher.Mono

class UserControllerTest extends Specification {

    UserController userController
    ApplicationProperties applicationProperties = Stub()
    UserCacheService userCacheService = Mock()

    def setup() {
        userController = new UserController(applicationProperties, userCacheService)
    }

    @Ignore
    def "getMe returns user information"() {
        given:
        Jwt jwt = Stub(Jwt) {
            getClaim("employeeId") >> "12345"
            getClaim("roles") >> ["https://role-catalog.vigoiks.no/vigo/elevfakturering/admin"]
        }
        applicationProperties.getDemo() >> false
        applicationProperties.getDemoUserEmployeeId() >> "12345"
        User testUser = new User()
        userCacheService.getUser(_, _) >> Mono.just(testUser)

        when:
        def responseMono = userController.getMe(jwt)

        then:
        1 * userCacheService.getUser(_, _)
        StepVerifier.create(responseMono)
                .assertNext { response ->
                    assert response.getStatusCode() == HttpStatus.OK
                }
                .verifyComplete()
    }

    @Ignore
    def "getMe returns BAD_REQUEST when employeeId is empty"() {
        given:
        Jwt jwt = Stub(Jwt)
        applicationProperties.getDemo() >> false
        applicationProperties.getDemoUserEmployeeId() >> ""

        when:
        def response = userController.getMe(jwt)

        then:
        1 * userCacheService.getUser(_, _) >> { throw new EmployeeIdException(HttpStatus.BAD_REQUEST, "Brukerautorisering mangler nødvendig informasjon (employeeId)!") }
        response.block().getStatusCode() == HttpStatus.BAD_REQUEST
    }

    def "ping returns ok response"() {
        when:
        def response = userController.ping()

        then:
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == "Greetings from FINTLabs :)"
    }
}
