package no.fint.betaling.model

import no.fint.betaling.service.OrderNumberService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import spock.lang.Specification

class BetalingFactorySpec extends Specification {
    private OrderNumberService orderNumberService
    private InvoiceFactory invoiceFactory
    private BetalingFactory betalingFactory

    void setup() {
        orderNumberService = Mock(OrderNumberService)
        invoiceFactory = Mock(InvoiceFactory) {
            getInvoice(_ as Betaling) >>
                    new FakturagrunnlagResource(
                            fakturalinjer: [new FakturalinjeResource(pris: 100L, antall: 2, fritekst: ['test'])],
                            ordrenummer: new Identifikator(identifikatorverdi: 'validorg0'),
                            total: 200L
                    )
        }
        betalingFactory = new BetalingFactory(orderNumberService: orderNumberService, invoiceFactory: invoiceFactory)
    }

    def "Get betaling given valid payment returns betaling"() {
        when:
        def betaling = betalingFactory.getBetaling(createPayment(), 'valid.org')

        then:
        1 * orderNumberService.getOrderNumber(_ as String) >> 'validorg0'
        betaling.get(0).ordrenummer == 'validorg0'
        betaling.get(0).fakturagrunnlag.fakturalinjer.get(0).pris == 100
    }

    private static Payment createPayment() {
        def payment = new Payment(
                customers: [new Kunde()],
                orderLines: [
                        new OrderLine(
                                orderLine: new VarelinjeResource(pris: 100L, kontering: new KontostrengResource(), enhet: 'enhet'),
                                amount: 1L,
                                description: 'test'
                        )],
                employer: new OppdragsgiverResource(navn: 'employer', systemId: new Identifikator(identifikatorverdi: 'test')),
                timeFrameDueDate: '14'
        )
        payment.orderLines[0].orderLine.addLink('self', Link.with('link.to.VarelinjeResource'))
        payment.employer.addLink('self', Link.with('link.to.OppdragsgiverResource'))
        return payment
    }
}
