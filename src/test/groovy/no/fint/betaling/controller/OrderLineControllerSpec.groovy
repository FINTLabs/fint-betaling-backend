package no.fint.betaling.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.model.Varelinje
import no.fint.betaling.service.OrderLineService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

class OrderLineControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private OrderLineService orderLineService
    private OrderLineController orderLineController

    void setup() {
        orderLineService = Mock(OrderLineService)
        orderLineController = new OrderLineController(orderLineService: orderLineService)
        mockMvc = standaloneSetup(orderLineController)
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
        orderLine.navn = 'testOrder'

        def mapper = new ObjectMapper()
        def jsonOrderLine = mapper.writeValueAsString(orderLine)

        when:
        def response = mockMvc.perform(post('/api/orderline/save').content(jsonOrderLine).contentType(MediaType.APPLICATION_JSON)
                .header('x-org-id', 'test.org'))

        then:
        1 * orderLineService.setOrderLine('test.org', _) >> orderLine
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.navn', 'testOrder'))
    }
}
