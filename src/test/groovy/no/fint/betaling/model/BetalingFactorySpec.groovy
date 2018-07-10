package no.fint.betaling.model

import no.fint.betaling.service.OrderNumberService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import spock.lang.Specification

class BetalingFactorySpec extends Specification {
    private OrderNumberService orderNumberService
    private BetalingFactory betalingFactory

    void setup() {
        GroovyMock(InvoiceFactory)
        orderNumberService = Mock(OrderNumberService)
        betalingFactory = new BetalingFactory(orderNumberService: orderNumberService)
    }
    /*
    def "Get betaling given valid payment returns betaling"() {
        given:

        def payment = new Payment(
                customers: [new Kunde()],
                orderLines: [new VarelinjeResource(pris: 100L, kontering: new KontostrengResource(), enhet: 'enhet')],
                employer: new OppdragsgiverResource(navn: 'employer', systemId: new Identifikator(identifikatorverdi: 'test')),
                timeFrameDueDate: '14'
        )

        when:
        def betaling = betalingFactory.getBetaling(payment, 'valid.org')

        then:
        1 * orderNumberService.getOrderNumber(_ as String) >> 'validorg0'
        1 * InvoiceFactory.getInvoice(_ as Betaling) >> createInvoice()
        betaling.get(0).ordrenummer == 'validorg0'
        betaling.get(0).fakturagrunnlag.fakturalinjer.get(0).pris == 100
    }
    */

    private static FakturagrunnlagResource createInvoice() {
        return new FakturagrunnlagResource(
                ordrenummer: new Identifikator(identifikatorverdi: 'validorg0'),
                fakturalinjer: [
                        new FakturalinjeResource(
                                antall: 1,
                                fritekst: ['invoiceline'],
                                pris: 100L
                        )
                ]
        )
    }
}
