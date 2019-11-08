package no.fint.betaling.factory

import no.fint.betaling.model.Claim
import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.model.OrderLine
import no.fint.betaling.repository.OrderNumberRepository
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource
import spock.lang.Specification

class ClaimFactorySpec extends Specification {
    private OrderNumberRepository orderNumberRepository
    private InvoiceFactory invoiceFactory
    private ClaimFactory claimFactory

    void setup() {
        orderNumberRepository = Mock(OrderNumberRepository)
        invoiceFactory = Mock(InvoiceFactory) {
            createInvoice(_ as Claim) >>
                    new FakturagrunnlagResource(
                            fakturalinjer: [new FakturalinjeResource(pris: 100L, antall: 2, fritekst: ['test'])],
                            ordrenummer: new Identifikator(identifikatorverdi: 'validorg0'),
                            netto: 200L
                    )
        }
        claimFactory = new ClaimFactory(orderNumberRepository: orderNumberRepository)
    }

    def "Get betaling given valid payment returns betaling"() {
        when:
        def claims = claimFactory.createClaim(createOrder(), 'valid.org')

        then:
        1 * orderNumberRepository.getOrderNumber(_ as String) >> 123
        claims.get(0).orderNumber == '123'
        claims.get(0).amountDue == 100
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
