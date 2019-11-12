package no.fint.betaling.factory

import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.model.OrderLine
import no.fint.betaling.repository.OrderNumberRepository
import spock.lang.Specification

class ClaimFactorySpec extends Specification {
    private OrderNumberRepository orderNumberRepository
    private ClaimFactory claimFactory

    void setup() {
        orderNumberRepository = Mock(OrderNumberRepository)
        claimFactory = new ClaimFactory(orderNumberRepository: orderNumberRepository)
    }

    def "Get betaling given valid payment returns betaling"() {
        when:
        def claims = claimFactory.createClaim(createOrder(), 'valid.org')

        then:
        1 * orderNumberRepository.getOrderNumber(_ as String) >> '123'
        claims.get(0).orderNumber == '123'
        claims.get(0).originalAmountDue == 100
    }

    private static Order createOrder() {
        def order = new Order(
                customers: [new Customer()],
                orderLines: [
                        new OrderLine(
                                itemUri: '/varelinje/123'.toURI(),
                                itemPrice: 100L,
                                numberOfItems: 1L,
                                description: 'test'
                        )],
                principalUri: '/oppdragsgiver/456'.toURI(),
                requestedNumberOfDaysToPaymentDeadline: '14'
        )
        return order
    }
}
