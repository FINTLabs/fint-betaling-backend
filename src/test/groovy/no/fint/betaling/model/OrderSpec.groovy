package no.fint.betaling.model

import spock.lang.Specification

class OrderSpec extends Specification {

    def "Sum should give the sum of all orderlines"() {

        given:
        def order = new Order(orderItems: [
                new OrderItem(lineitem: new Lineitem(itemPrice: 10), itemQuantity: 5),
                new OrderItem(lineitem: new Lineitem(itemPrice: 2), itemPrice: 2, itemQuantity: 10)
        ])

        when:
        def sum = order.sum()

        then:
        sum == 70
    }
}
