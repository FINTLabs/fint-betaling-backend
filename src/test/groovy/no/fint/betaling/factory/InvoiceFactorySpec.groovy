package no.fint.betaling.factory

import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Customer
import no.fint.betaling.model.OrderLine
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
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
        invoice.netto == 10000
        invoice.fakturalinjer.size() == 1
    }
}


