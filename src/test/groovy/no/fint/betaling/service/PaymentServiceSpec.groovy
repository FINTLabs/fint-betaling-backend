package no.fint.betaling.service

import no.fint.betaling.model.Betaling

import no.fint.betaling.model.Kunde
import no.fint.model.administrasjon.kompleksedatatyper.Kontostreng
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag
import no.fint.model.administrasjon.okonomi.Varelinje
import no.fint.model.felles.kompleksedatatyper.Personnavn
import spock.lang.Specification

class PaymentServiceSpec extends Specification {
    private String orgId
    private MongoService mongoService
    private OrderNumberService ordernumberService
    private PaymentService paymentService

    void setup() {
        orgId = 'test.no'
        mongoService = Mock(MongoService)
        ordernumberService = Mock(OrderNumberService)
        paymentService = new PaymentService(mongoService: mongoService, orderNumberService: ordernumberService)
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
        def listBetaling = paymentService.getPaymentsByLastname(orgId, 'Correctlastname')

        then:
        1 * mongoService.getPayments('test.no', _) >> [createPayment('123', 'Correctlastname')]
        listBetaling.size() == 1
        listBetaling.get(0).kunde.navn.etternavn == 'Correctlastname'
    }

    def "Get payment given valid ordernumber returns list with payments matching given ordernumber"(){
        when:
        def listBetaling = paymentService.getPaymentsByOrdernumber(orgId, '5')

        then:
        1 * ordernumberService.getOrderNumberFromNumber(orgId, _) >> 'testno5'
        1 * mongoService.getPayments('test.no', _) >> [createPayment('testno5', 'Testesen')]
        listBetaling.size() == 1
        listBetaling.get(0).ordrenummer == 'testno5'
    }

    def "Save payment given valid data returns void"() {
        given:
        def orderLine = new Varelinje(pris: 100L, enhet: "enhet", kontering: new Kontostreng())
        def customer = new Kunde(navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))

        when:
        def response = paymentService.setPayment(orgId, [orderLine], [customer])

        then:
        1 * ordernumberService.getOrderNumber(_) >> 'testNummer'
        1 * mongoService.setPayment('test.no', _)
        response == null
    }

    private static Betaling createPayment(String ordernumber, String lastname) {
        return new Betaling(kunde: new Kunde(navn: new Personnavn(etternavn: lastname)), ordrenummer: ordernumber);
    }
}
