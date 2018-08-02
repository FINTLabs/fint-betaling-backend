package no.fint.betaling.model


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
        def invoice = invoiceFactory.getInvoice(createPayment())

        then:
        invoice.ordrenummer.identifikatorverdi == 'test0'
        invoice.netto == 1L
        invoice.fakturalinjer.size() == 1
    }

    private static Betaling createPayment() {
        def employer = new OppdragsgiverResource()
        employer.setNavn('test employer')
        employer.addLink('self', new Link('link.to.Oppdragsgiver'))
        def varelinjeResource = new VarelinjeResource()
        varelinjeResource.setEnhet('enhet')
        varelinjeResource.setKontering(new KontostrengResource())
        varelinjeResource.setPris(1L)
        varelinjeResource.addLink('self', new Link('link.to.VarelinjeResource'))
        def orderLine = new OrderLine(orderLine: varelinjeResource, amount: 1, description: 'test')
        def customer = new Kunde(
                person: new Link('link.to.PersonResource')
        )
        return new Betaling(
                varelinjer: [orderLine],
                kunde: customer,
                ordrenummer: 'test0',
                oppdragsgiver: employer,
                timeFrameDueDate: '7'
        )
    }
}


