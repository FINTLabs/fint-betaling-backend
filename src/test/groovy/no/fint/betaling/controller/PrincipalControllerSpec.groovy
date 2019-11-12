package no.fint.betaling.controller

import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class PrincipalControllerSpec extends MockMvcSpecification {
    private PrincipalController controller
    private RestUtil restUtil
    private MockMvc mockMvc

    void setup() {
        restUtil = Mock(RestUtil)
        controller = new PrincipalController(restUtil: restUtil)
        mockMvc = standaloneSetup(controller)
    }

    def "Get employers given valid org id returns list"() {
        given:
        def oppdragsgiverResources = new OppdragsgiverResources()
        def oppdragsgiverResource = new OppdragsgiverResource(navn: 'test', systemId: new Identifikator(identifikatorverdi: 'test'))
        oppdragsgiverResources.addResource(oppdragsgiverResource)

        when:
        def response = mockMvc.perform(get('/api/principal').header('x-org-id', 'valid.org'))

        then:
        1 * restUtil.get(_, _, _) >> oppdragsgiverResources

        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'test'))
    }
}
