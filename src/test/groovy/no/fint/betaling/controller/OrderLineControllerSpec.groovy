package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import no.fint.betaling.service.OrderLineService
import no.fint.model.administrasjon.kompleksedatatyper.Kontostreng
import no.fint.model.administrasjon.okonomi.Varelinje
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class OrderLineControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private OrderLineService orderLineService
    private OrderLineController orderLineController
    private ObjectMapper mapper

    void setup() {
        orderLineService = Mock(OrderLineService)
        orderLineController = new OrderLineController(orderLineService: orderLineService)

        def converter = new MappingJackson2HttpMessageConverter()
        mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        converter.setObjectMapper(mapper)
        def mockMvcBuilder = MockMvcBuilders.standaloneSetup(orderLineController).setMessageConverters(converter)
        mockMvc = mockMvcBuilder.build()
    }


    def "Get all order lines returns list of Varelinje"() {
        when:
        def response = mockMvc.perform(get('/api/orderline').header('x-org-id', 'test.org'))

        then:
        1 * orderLineService.getOrderLines('test.org') >> [new Varelinje()]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
    }

    def "Set order line given valid order line returns order line"() {
        given:
        def orderLine = new Varelinje()
        orderLine.setNavn('testOrder')
        orderLine.setEnhet('enhet')
        orderLine.setKontering(new Kontostreng())
        orderLine.setPris(1000)
        orderLine.setKode('kode')
        def identifikator = new Identifikator(identifikatorverdi: 'test')
        orderLine.setSystemId(identifikator)

        def jsonOrderLine = mapper.writeValueAsString(orderLine)

        when:
        def response = mockMvc.perform(post('/api/orderline/save').content(jsonOrderLine).contentType(MediaType.APPLICATION_JSON)
                .header('x-org-id', 'test.org'))
        System.out.println(response)


        then:
        1 * orderLineService.setOrderLine('test.org', _) >> true
        response.andExpect(status().isOk())
    }
}
