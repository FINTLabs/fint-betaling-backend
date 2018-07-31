package no.fint.betaling.service

import no.fint.betaling.model.*
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

class PaymentServiceSpec extends Specification {
    private String orgId
    private MongoService mongoService
    private OrderNumberService ordernumberService
    private PaymentService paymentService
    private BetalingFactory betalingFactory

    void setup() {
        orgId = 'test.no'
        mongoService = Mock(MongoService)
        ordernumberService = Mock(OrderNumberService)
        betalingFactory = Mock(BetalingFactory)
        paymentService = new PaymentService(mongoService: mongoService, orderNumberService: ordernumberService, betalingFactory: betalingFactory)
    }

    def "Get all payments given valid orgId returns list"() {
        when:
        def listBetaling = paymentService.getAllPayments(orgId)

        then:
        1 * mongoService.getPayments('test.no', _) >> [new Betaling(), new Betaling()]
        listBetaling.size() == 2
    }

    def "Get payment by name given valid lastname returns list with payments matching given lastname"() {
        when:
        def listBetaling = paymentService.getPaymentsByCustomerName(orgId, 'Correctlastname')

        then:
        1 * mongoService.getPayments('test.no', _) >> [createPayment('123', 'Correctlastname')]
        listBetaling.size() == 1
        listBetaling.get(0).kunde.navn == 'Correctlastname, Test'
    }

    def "Get payment given valid ordernumber returns list with payments matching given ordernumber"() {
        when:
        def listBetaling = paymentService.getPaymentsByOrdernumber(orgId, '5')

        then:
        1 * ordernumberService.getOrderNumberFromNumber(orgId, _) >> 'testno5'
        1 * mongoService.getPayments('test.no', _ as Query) >> [createPayment('testno5', 'Testesen')]
        listBetaling.size() == 1
        listBetaling.get(0).ordrenummer == 'testno5'
    }

    def "Save payment given valid data returns void"() {
        given:
        def orderLine = new OrderLine(
                orderLine: new VarelinjeResource(pris: 100L, enhet: "enhet", kontering: new KontostrengResource()),
                amount: 1,
                description: 'test'
        )
        def customer = new Kunde(navn: 'Testesen, Ola')
        def employer = new OppdragsgiverResource(navn: 'Emp Loyer', systemId: new Identifikator(identifikatorverdi: 'test'))
        def payment = new Payment(employer: employer, orderLines: [orderLine], customers: [customer], timeFrameDueDate: 7L)

        when:
        def response = paymentService.setPayment(orgId, payment)

        then:
        1 * betalingFactory.getBetaling(_ as Payment, 'test.no') >> [createPayment('testno0','Testesen')]
        1 * mongoService.setPayment('test.no', _ as Betaling)
        response.size() == 1
        response.get(0).varelinjer.size() == 1
        response.get(0).ordrenummer == 'testno0'
        response.get(0).kunde.navn == 'Testesen, Test'
    }

    private static Betaling createPayment(String ordernumber, String lastname) {
        def employer = new OppdragsgiverResource()
        employer.setNavn('test employer')
        employer.addLink('self', new Link('link.to.Oppdragsgiver'))
        def varelinjeResource = new VarelinjeResource()
        varelinjeResource.setEnhet('enhet')
        varelinjeResource.setKontering(new KontostrengResource())
        varelinjeResource.setPris(1L)
        def orderLine = new OrderLine(orderLine: varelinjeResource, amount: 1, description: 'test')
        def customer = new Kunde(
                navn: "${lastname}, Test"
        )
        return new Betaling(
                varelinjer: [orderLine],
                kunde: customer,
                ordrenummer: ordernumber,
                oppdragsgiver: employer,
                timeFrameDueDate: '7'
        )
    }

}
