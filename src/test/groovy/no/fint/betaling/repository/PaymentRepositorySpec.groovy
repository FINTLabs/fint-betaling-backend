package no.fint.betaling.repository

import no.fint.betaling.factory.ClaimFactory
import no.fint.betaling.model.*
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

class PaymentRepositorySpec extends Specification {
    private String orgId
    private MongoRepository mongoRepository
    private PaymentRepository paymentRepository
    private ClaimFactory betalingFactory

    void setup() {
        orgId = 'test.no'
        mongoRepository = Mock(MongoRepository)
        betalingFactory = Mock(ClaimFactory)
        paymentRepository = new PaymentRepository(mongoRepository: mongoRepository, claimFactory: betalingFactory)
    }

    def "Get all payments given valid orgId returns list"() {
        when:
        def listBetaling = paymentRepository.getAllPayments(orgId)

        then:
        1 * mongoRepository.getPayments('test.no', _) >> [new Betaling(), new Betaling()]
        listBetaling.size() == 2
    }

    def "Get payment by name given valid lastname returns list with payments matching given lastname"() {
        when:
        def listBetaling = paymentRepository.getPaymentsByCustomerName(orgId, 'Correctlastname')

        then:
        1 * mongoRepository.getPayments('test.no', _) >> [createPayment(123, 'Correctlastname')]
        listBetaling.size() == 1
        listBetaling.get(0).kunde.navn.etternavn == 'Correctlastname'
    }

    def "Get payment given valid ordernumber returns list with payments matching given ordernumber"() {
        when:
        def listBetaling = paymentRepository.getPaymentsByOrdernumber(orgId, '5')

        then:
        1 * mongoRepository.getPayments('test.no', _ as Query) >> [createPayment(124, 'Testesen')]
        listBetaling.size() == 1
        listBetaling.get(0).ordrenummer == 124
    }

    def "Save payment given valid data returns void"() {
        given:
        def orderLine = new OrderLine(
                orderLine: new VarelinjeResource(pris: 100L, enhet: "enhet", kontering: new KontostrengResource()),
                amount: 1,
                description: 'test'
        )
        def customer = new Kunde(navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))
        def employer = new OppdragsgiverResource(navn: 'Emp Loyer', systemId: new Identifikator(identifikatorverdi: 'test'))
        def payment = new Payment(employer: employer, orderLines: [orderLine], customers: [customer], timeFrameDueDate: 7L)

        when:
        def response = paymentRepository.setPayment(orgId, payment)

        then:
        1 * betalingFactory.createClaim(_ as Payment, 'test.no') >> [createPayment(123,'Testesen')]
        1 * mongoRepository.setPayment('test.no', _ as Betaling)
        response.size() == 1
        response.get(0).varelinjer.size() == 1
        response.get(0).ordrenummer == 123
        response.get(0).kunde.navn.etternavn == 'Testesen'
    }

    private static Betaling createPayment(long ordernumber, String lastname) {
        def employer = new OppdragsgiverResource()
        employer.setNavn('test employer')
        employer.addLink('self', new Link('link.to.Oppdragsgiver'))
        def varelinjeResource = new VarelinjeResource()
        varelinjeResource.setEnhet('enhet')
        varelinjeResource.setKontering(new KontostrengResource())
        varelinjeResource.setPris(1L)
        def orderLine = new OrderLine(orderLine: varelinjeResource, amount: 1, description: 'test')
        def customer = new Kunde(
                navn: new Personnavn(fornavn: 'Test', etternavn: lastname)
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
