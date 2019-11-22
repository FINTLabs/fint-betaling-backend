package no.fint.betaling.controller

import no.fint.betaling.model.Principal
import no.fint.betaling.repository.PrincipalRepository
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class PrincipalControllerSpec extends MockMvcSpecification {
    private PrincipalController controller
    private MockMvc mockMvc
    private PrincipalRepository repository

    void setup() {
        repository = Mock()
        controller = new PrincipalController(repository: repository)
        mockMvc = standaloneSetup(controller)
    }

    def "Get employers given valid org id returns list"() {
        when:
        def response = mockMvc.perform(get('/api/principal'))

        then:
        1 * repository.getPrincipals() >> [new Principal(description: 'test')]

        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].description', 'test'))
    }
}
