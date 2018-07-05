package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import no.fint.betaling.service.RestService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class OrderLineControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private RestService restService
    private OrderLineController orderLineController
    private ObjectMapper mapper

    void setup() {
        restService = Mock(RestService)
        orderLineController = new OrderLineController(restService: restService, orderLineEndpoint: 'endpoints/orderLine')

        def converter = new MappingJackson2HttpMessageConverter()
        mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        converter.setObjectMapper(mapper)
        def mockMvcBuilder = MockMvcBuilders.standaloneSetup(orderLineController).setMessageConverters(converter)
        mockMvc = mockMvcBuilder.build()
    }


    def "Get all order lines returns list of Varelinje"() {
        given:
        def resources = new VarelinjeResources()
        resources.addResource(createOrderLineResource())

        when:
        def response = mockMvc.perform(get('/api/orderline').header('x-org-id', 'test.org'))

        then:
        1 * restService.getResource(_, _, 'test.org') >> resources
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testOrder'))
    }

    def "Set order line given valid order line returns order line"() {
        given:
        def jsonOrderLine = mapper.writeValueAsString(createOrderLineResource())

        when:
        def response = mockMvc.perform(post('/api/orderline').content(jsonOrderLine).contentType(MediaType.APPLICATION_JSON)
                .header('x-org-id', 'test.org'))
        System.out.println(response)


        then:
        1 * restService.setResource(_, _, _, 'test.org') >> true
        response.andExpect(status().isOk())
    }

    private static VarelinjeResource createOrderLineResource() {
        def orderLine = new VarelinjeResource()
        orderLine.setNavn('testOrder')
        orderLine.setEnhet('enhet')
        orderLine.setKontering(new KontostrengResource())
        orderLine.setPris(1000)
        orderLine.setKode('kode')
        def identifikator = new Identifikator(identifikatorverdi: 'test')
        orderLine.setSystemId(identifikator)
        return orderLine
    }
}
