package no.fint.betaling.factory

import no.fint.betaling.repository.OrderNumberRepository
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Specification

class ClaimFactorySpec extends Specification {
    private OrderNumberRepository orderNumberRepository
    private ClaimFactory claimFactory
    private BetalingObjectFactory betalingObjectFactory

    void setup() {
        orderNumberRepository = Mock(OrderNumberRepository)
        claimFactory = new ClaimFactory(orderNumberRepository: orderNumberRepository)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Get betaling given valid payment returns betaling"() {
        given:
        def order = betalingObjectFactory.newOrder()

        when:
        def claims = claimFactory.createClaim(order)

        then:
        1 * orderNumberRepository.getHighestOrderNumber() >> 123L
        claims.get(0).orderNumber == '124'
        claims.get(0).originalAmountDue == 10000
    }
}
