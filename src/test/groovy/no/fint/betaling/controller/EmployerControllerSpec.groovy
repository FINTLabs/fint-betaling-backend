package no.fint.betaling.controller

import no.fint.betaling.service.RestService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class EmployerControllerSpec extends MockMvcSpecification {
    private EmployerController controller
    private RestService restService
    private MockMvc mockMvc

    void setup() {
        restService = Mock(RestService)
        controller = new EmployerController(restService: restService, employerEndpoint: 'endpoints/employer')
        mockMvc = standaloneSetup(controller)
    }

    def "Get employers given valid org id returns list"() {
        given:
        def oppdragsgiverResources = new OppdragsgiverResources()
        oppdragsgiverResources.addResource(new OppdragsgiverResource(navn: 'test', systemId: new Identifikator(identifikatorverdi: 'test')))

        when:
        def response = mockMvc.perform(get('/api/oppdragsgiver')
                .header('x-org-id', 'valid.org'))

        then:
        1 * restService.getResource(_ as Class<OppdragsgiverResources>, _ as String, _ as String) >>
                oppdragsgiverResources

        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'test'))
    }
}
