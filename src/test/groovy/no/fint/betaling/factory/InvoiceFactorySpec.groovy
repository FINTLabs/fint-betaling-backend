package no.fint.betaling.factory

import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.service.PersonService
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.model.resource.Link
import spock.lang.Specification

import java.time.LocalDate
import java.time.ZoneId

class InvoiceFactorySpec extends Specification {
    private BetalingObjectFactory betalingObjectFactory
    private InvoiceFactory invoiceFactory
    private PersonService personService

    void setup() {
        betalingObjectFactory = new BetalingObjectFactory()
        personService = Mock()
        invoiceFactory = new InvoiceFactory(personService)
    }

    def "Get invoice given valid payment returns invoice"() {
        given:
        def claim = betalingObjectFactory.newClaim(123L, ClaimStatus.STORED)

        when:
        def invoice = invoiceFactory.createInvoice(claim)

        then:
        invoice.ordrenummer.identifikatorverdi == '123'
        invoice.leveringsdato == Date.from(LocalDate.parse('2019-11-01').atStartOfDay(ZoneId.systemDefault()).toInstant())
        invoice.nettobelop == 1000000
        invoice.fakturalinjer.size() == 1
        invoice.fakturalinjer.get(0).fritekst == ['Monkeyballs']
        invoice.links.get("fakturautsteder").get(0).getHref() ==~ /.*\\/title\/tt0093780\//
    }
}


