package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import no.fint.betaling.model.Lineitem
import no.fint.betaling.repository.LineitemRepository
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class LineitemControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private LineitemController lineitemController
    private LineitemRepository lineitemRepository
    private ObjectMapper mapper

    void setup() {
        lineitemRepository = Mock()
        lineitemController = new LineitemController(repository: lineitemRepository)

        def converter = new MappingJackson2HttpMessageConverter()
        mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        converter.setObjectMapper(mapper)
        def mockMvcBuilder = MockMvcBuilders.standaloneSetup(lineitemController).setMessageConverters(converter)
        mockMvc = mockMvcBuilder.build()
    }

    def "Get all order lines returns list of Varelinje"() {
        when:
        def response = mockMvc.perform(get('/api/lineitem'))

        then:
        1 * lineitemRepository.getLineitems() >> [new Lineitem(code: 'XCC', description: 'Hello there', itemPrice: 12.00, taxrate: 0.25)]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].description', 'Hello there'))
    }

}
