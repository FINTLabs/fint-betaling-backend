package no.fint.betaling.service

import no.fint.model.administrasjon.okonomi.Varelinje
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class OrderLineServiceSpec extends Specification {

    private RestTemplate restTemplate
    private OrderLineService orderLineService

    void setup() {
        restTemplate = Mock(RestTemplate)
        orderLineService = new OrderLineService(restTemplate: restTemplate)
    }

    def "Get order lines given valid orgId returns list of Varelinje"() {
        when:
        def orderLines = orderLineService.getOrderLines('valid.org')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok([new Varelinje()])
        orderLines.size() == 1
        orderLines.get(0) instanceof Varelinje
    }

    def "Set order line given valid ordId and valid order line returns true"() {
        when:
        def didSet = orderLineService.setOrderLine('valid.org', new Varelinje(navn: 'valid order'))

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.status(HttpStatus.OK).build()
        didSet
    }

    def "Get order lines given invalid orgId returns empty list"() {
        when:
        def orderLines = orderLineService.getOrderLines('invalid.org')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok([])
        orderLines.size() == 0
    }
}