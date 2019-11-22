package no.fint.betaling.factory


import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Specification

class InvoiceFactorySpec extends Specification {
    private BetalingObjectFactory betalingObjectFactory

    void setup() {
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Get invoice given valid payment returns invoice"() {
        given:
        def claim = betalingObjectFactory.newClaim('123', ClaimStatus.STORED)

        when:
        def invoice = InvoiceFactory.createInvoice(claim)

        then:
        invoice.ordrenummer.identifikatorverdi == '123'
        invoice.netto == 1000000
        invoice.fakturalinjer.size() == 1
    }
}


