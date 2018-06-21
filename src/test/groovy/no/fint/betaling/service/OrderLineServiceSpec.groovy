package no.fint.betaling.service

import no.fint.model.administrasjon.okonomi.Varelinje
import spock.lang.Specification

class OrderLineServiceSpec extends Specification {

    private MongoService mongoService;
    private OrderLineService orderLineService;

    void setup() {
        mongoService = Mock(MongoService)
        orderLineService = new OrderLineService(mongoService: mongoService)
    }

    def "Get order lines given valid orgId returns list of Varelinje"() {
        when:
        def orderLines = orderLineService.getOrderLines('valid.org')

        then:
        1 * mongoService.getOrderLine('valid.org', _) >> [new Varelinje()]
        orderLines.size() == 1
        orderLines.get(0) instanceof Varelinje
    }

    def "Set order line given valid ordId and valid order line returns Varelinje"() {
        when:
        def orderLine = orderLineService.setOrderLine('valid.org', new Varelinje(navn: 'valid order'))

        then:
        1 * mongoService.setOrderLine('valid.org', _)
        orderLine.navn == 'valid order'
    }

    def "Get order lines given invalid orgId returns empty list"() {
        when:
        def orderLines = orderLineService.getOrderLines('invalid.org')

        then:
        1 * mongoService.getOrderLine('invalid.org', _) >> []
        orderLines.size() == 0
    }
}
