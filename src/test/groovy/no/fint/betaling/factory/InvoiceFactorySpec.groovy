package no.fint.betaling.factory


import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import spock.lang.Specification

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

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
        invoice.leveringsdato == Date.from(LocalDate.parse('2019-11-01').atStartOfDay(ZoneId.systemDefault()).toInstant())
        invoice.netto == 1000000
        invoice.fakturalinjer.size() == 1
        invoice.fakturalinjer.get(0).fritekst == ['Monkeyballs']
        invoice.mottaker.any { it.href == 'link.to.Person' }
        invoice.oppdragsgiver.any { it.href ==~ /.*\\/title\/tt0093780\// }
    }
}


