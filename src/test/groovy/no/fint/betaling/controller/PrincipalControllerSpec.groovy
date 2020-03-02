package no.fint.betaling.controller

import no.fint.betaling.model.Principal
import no.fint.betaling.service.PrincipalService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class PrincipalControllerSpec extends MockMvcSpecification {
    private PrincipalController controller
    private MockMvc mockMvc
    private PrincipalService principalService

    void setup() {
        principalService = Mock()
        controller = new PrincipalController(principalService)
        mockMvc = standaloneSetup(controller)
    }

    def "Get employers given valid org id returns list"() {
        when:
        def response = mockMvc.perform(get('/api/principal').header('x-school-org-id', '12345'))

        then:
        1 * principalService.getPrincipalByOrganisationId('12345') >> new Principal(description: 'test')

        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.description', 'test'))
    }
}
