package no.fint.betaling.controller

import no.fint.betaling.repository.MeRepository
import spock.lang.Ignore
import spock.lang.Specification

class MeControllerSpec extends Specification {

    @Ignore
    def "Get me returns user"() {
        given:
        def controller = new MeController(Mock(MeRepository))
        def mockMvc = standaloneSetup(controller)

        when:
        def response = mockMvc.perform(get('/api/me'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.name', 'Navn Navnesen'))
    }
}
