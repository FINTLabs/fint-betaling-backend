package no.fint.betaling.controller

import no.fint.betaling.repository.MeRepository
import no.fint.test.utils.MockMvcSpecification
import spock.lang.Ignore

class MeControllerSpec extends MockMvcSpecification {

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
