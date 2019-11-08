package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class LineitemControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private RestUtil restUtil
    private LineitemController lineitemController
    private ObjectMapper mapper

    void setup() {
        restUtil = Mock(RestUtil)
        lineitemController = new LineitemController(restUtil: restUtil, lineitemEndpoint: 'endpoints/lineitem'.toURI())

        def converter = new MappingJackson2HttpMessageConverter()
        mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        converter.setObjectMapper(mapper)
        def mockMvcBuilder = MockMvcBuilders.standaloneSetup(lineitemController).setMessageConverters(converter)
        mockMvc = mockMvcBuilder.build()
    }

    def "Get all order lines returns list of Varelinje"() {
        given:
        def resources = new VarelinjeResources()
        resources.addResource(createOrderLineResource())

        when:
        def response = mockMvc.perform(get('/api/lineitem').header('x-org-id', 'test.org'))

        then:
        1 * restUtil.get(_, _, 'test.org') >> resources
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testOrder'))
    }

    def "Set order line given valid order line returns order line"() {
        given:
        def jsonLineitem = mapper.writeValueAsString(createOrderLineResource())

        when:
        def response = mockMvc.perform(post('/api/lineitem').content(jsonLineitem).contentType(MediaType.APPLICATION_JSON)
                .header('x-org-id', 'test.org'))
        System.out.println(response)

        then:
        1 * restUtil.post(_, _, _, 'test.org') >> ResponseEntity.ok().build()
        response.andExpect(status().isOk())
    }

    private static VarelinjeResource createOrderLineResource() {
        def orderLine = new VarelinjeResource()
        orderLine.setNavn('testOrder')
        orderLine.setEnhet('unit')
        orderLine.setKontering(new KontostrengResource())
        orderLine.setPris(1000)
        orderLine.setKode('code')
        def identifikator = new Identifikator(identifikatorverdi: 'test')
        orderLine.setSystemId(identifikator)
        return orderLine
    }
}
