package no.fint.betaling.controller

import no.fint.betaling.model.Taxcode
import no.fint.betaling.repository.TaxcodeRepository
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

class TaxcodeControllerSpec extends Specification {
    private MockMvc mockMvc
    private TaxcodeController controller
    private TaxcodeRepository repository

    void setup() {
        repository = Mock()
        controller = new TaxcodeController(repository: repository)
        mockMvc = standaloneSetup(controller)
    }

    def "Get mva codes given valid org id returns list"() {
        when:
        def response = mockMvc.perform(get('/api/mva-code'))

        then:
        1 * repository.getTaxcodes() >> [new Taxcode(code: '25%', description: 'Høy rate', rate: 0.25)]

        response.andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].code', '25%'))
    }
}
