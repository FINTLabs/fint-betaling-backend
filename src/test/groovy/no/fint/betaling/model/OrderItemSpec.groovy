package no.fint.betaling.model

import spock.lang.Specification

class OrderItemSpec extends Specification {

    def "Sum should return the product of price and amount"() {

        given:
        def line = new OrderItem(lineitem: new Lineitem(itemPrice: 10), itemPrice: 10, itemQuantity: 10)

        when:
        def sum = line.sum()

        then:
        sum == 100
    }
}
