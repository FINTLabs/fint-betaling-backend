package no.fint.betaling.factory

import no.fint.betaling.model.Claim
import no.fint.betaling.model.Customer
import no.fint.betaling.model.OrderLine
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import spock.lang.Specification

class InvoiceFactorySpec extends Specification {

    private InvoiceFactory invoiceFactory

    void setup() {
        invoiceFactory = new InvoiceFactory()
    }

    def "Get invoice given valid payment returns invoice"() {
        when:
        def invoice = invoiceFactory.createInvoice(createPayment())

        then:
        invoice.ordrenummer.identifikatorverdi == '123'
        invoice.netto == 1L
        invoice.fakturalinjer.size() == 1
    }

    private static Claim createPayment() {
        def varelinjeResource = new VarelinjeResource()
        varelinjeResource.setEnhet('unit')
        varelinjeResource.setKontering(new KontostrengResource())
        varelinjeResource.setPris(1L)
        varelinjeResource.addLink('self', new Link('link.to.VarelinjeResource'))
        def orderLine = new OrderLine(numberOfItems: 1L, itemUri: 'link.to.VarelinjeResource'.toURI(), itemPrice: 1L, description: 'test')
        def customer = new Customer(name: 'Testesen', person: 'link.to.PersonResource'.toURI())

        return new Claim(
                orderLines: [orderLine],
                customer: customer,
                orderNumber: '123',
                principalUri: 'link.to.Oppdragsgiver'.toURI(),
                requestedNumberOfDaysToPaymentDeadline: '7'
        )
    }
}


