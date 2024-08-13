package no.fint.betaling.controller

import no.fint.betaling.common.config.ApplicationProperties
import no.fint.betaling.common.exception.EmployeeIdException
import no.fint.betaling.model.User
import no.fint.betaling.user.UserCacheService
import no.fint.betaling.user.UserController
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class UserControllerTest extends Specification {

    UserController userController
    ApplicationProperties applicationProperties = Stub()
    UserCacheService userCacheService = Mock()

    def setup() {
        userController = new UserController(applicationProperties, userCacheService)
    }

    def "getMe returns user information"() {
        given:
        Jwt jwt = Stub(Jwt) {
            getClaimAsString("employeeId") >> "12345"
            getClaimAsStringList("roles") >> ["https://role-catalog.vigoiks.no/vigo/elevfakturering/admin"]
        }

        applicationProperties.getDemo() >> false
        applicationProperties.getDemoUserEmployeeId() >> "12345"
        User testUser = new User()

        when:
        def responseMono = userController.getMe(jwt)

        then:
        1 * userCacheService.getUser(_, _) >> Mono.just(testUser)
        StepVerifier.create(responseMono)
                .assertNext { response ->
                    assert response.getStatusCode() == HttpStatus.OK
                    assert response.getBody() == testUser
                }
                .verifyComplete()
    }

    def "getMe returns BAD_REQUEST when employeeId is empty"() {
        given:
        Jwt jwt = Stub(Jwt) {
            getClaimAsString("employeeId") >> ""
        }
        applicationProperties.getDemo() >> false

        when:
        def responseMono = userController.getMe(jwt)

        then:
        0 * userCacheService.getUser(_, _)
        StepVerifier.create(responseMono)
                .expectErrorMatches { throwable ->
                    throwable instanceof EmployeeIdException &&
                            throwable.getStatusCode() == HttpStatus.BAD_REQUEST &&
                            throwable.getMessage().contains("Brukerautorisering mangler nødvendig informasjon (employeeId)!")
                }
                .verify()
    }

    def "ping returns ok response"() {
        when:
        def response = userController.ping()

        then:
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == "Greetings from FINTLabs :)"
    }
}
