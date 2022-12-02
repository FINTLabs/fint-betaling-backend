package no.fint.betaling.controller

import no.fint.betaling.repository.UserRepository
import spock.lang.Ignore
import spock.lang.Specification

class UserControllerSpec extends Specification {

    @Ignore
    def "Get me returns user"() {
        given:
        def controller = new UserController(Mock(UserRepository), applicationProperties)
        def mockMvc = standaloneSetup(controller)

        when:
        def response = mockMvc.perform(get('/api/me'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.name', 'Navn Navnesen'))
    }
}
