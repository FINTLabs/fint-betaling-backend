package no.fint.betaling.controller

import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.okonomi.MvakodeResource
import no.fint.model.resource.administrasjon.okonomi.MvakodeResources
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class MvaCodeControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private RestUtil restUtil
    private MvaCodeController controller

    void setup() {
        restUtil = Mock(RestUtil)
        controller = new MvaCodeController(restUtil: restUtil)
        mockMvc = standaloneSetup(controller)
    }

    def "Get mva codes given valid org id returns list"() {
        given:
        def mvaCodes = new MvakodeResources()
        def mvaCode = new MvakodeResource(kode: '25', navn: '25%', systemId: new Identifikator(identifikatorverdi: 'test'))
        mvaCodes.addResource(mvaCode)

        when:
        def response = mockMvc.perform(get('/api/mva-code').header('x-org-id', 'test.org'))

        then:
        1 * restUtil.get(_, _, _) >> mvaCodes

        response.andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].kode', '25'))
                .andExpect(jsonPathEquals('$[0].navn', '25%'))
    }
}
