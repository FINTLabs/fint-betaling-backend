package no.fint.betaling.service

import no.fint.betaling.model.Betaling
import no.fint.betaling.model.Kunde
import no.fint.model.felles.kompleksedatatyper.Personnavn
import spock.lang.Specification

class PaymentServiceSpec extends Specification {
    private String orgId
    private MongoService mongoService
    private PaymentService paymentService

    void setup() {
        orgId = 'test.no'
        mongoService = Mock(MongoService)
        paymentService = new PaymentService(mongoService: mongoService)
    }

    def "Get all payments given valid orgId returns list"() {
        when:
        def listBetaling = paymentService.getAllPayments(orgId)

        then:
        1 * mongoService.getFakturagrunnlag('test.no', _) >> [new Betaling(), new Betaling()]
        listBetaling.size() == 2
    }

    def "Get payment by name given valid lastname returns list with payments matching given lastname"() {
        when:
        def listBetaling = paymentService.getPaymentsByLastname(orgId, 'Correctlastname')

        then:
        1 * mongoService.getFakturagrunnlag('test.no', _) >> [createPayment('123', 'Correctlastname')]
        listBetaling.size() == 1
        listBetaling.get(0).kunde.navn.etternavn == 'Correctlastname'
    }

    def "Get payment given valid ordernumber returns list with payments matching given ordernumber"(){
        when:
        def listBetaling = paymentService.getPaymentsByOrdernumber(orgId, 'validOrdernumber')

        then:
        1 * mongoService.getFakturagrunnlag('test.no', _) >> [createPayment('validOrdernumber', 'Testesen')]
        listBetaling.size() == 1
        listBetaling.get(0).ordrenummer == 'validOrdernumber'
    }

    private static Betaling createPayment(String ordernumber, String lastname) {
        return new Betaling(kunde: new Kunde(navn: new Personnavn(etternavn: lastname)), ordrenummer: ordernumber);
    }
}
