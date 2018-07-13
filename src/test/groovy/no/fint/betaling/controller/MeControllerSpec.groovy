package no.fint.betaling.controller

import no.fint.test.utils.MockMvcSpecification

class MeControllerSpec extends MockMvcSpecification {
    def "Get me returns user"() {
        given:
        def controller = new MeController()
        def mockMvc = standaloneSetup(controller)

        when:
        def response = mockMvc.perform(get('/api/me'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.name', 'Ola Nordmann'))
                .andExpect(jsonPathEquals('$.organisation', 'Rogaland fylkeskommune'))
    }
}
