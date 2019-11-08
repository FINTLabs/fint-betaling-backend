package no.fint.betaling.repository

import no.fint.betaling.factory.ClaimFactory
import no.fint.betaling.model.*
import no.fint.betaling.service.ClaimService
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
    private ClaimRepository claimRepository
    private ClaimService claimService
    private ClaimFactory claimFactory

    void setup() {
        orgId = 'test.no'
        claimRepository = Mock(ClaimRepository)
        claimFactory = Mock(ClaimFactory)
        claimService = new ClaimService(claimRepository: claimRepository, claimFactory: claimFactory)
    }

    def "Get all payments given valid orgId returns list"() {
        when:
        def claims = claimService.getAllClaims(orgId)

        then:
        1 * claimRepository.getClaims('test.no', _) >> [new Claim(), new Claim()]
        claims.size() == 2
    }

    def "Get payment by name given valid lastname returns list with payments matching given lastname"() {
        when:
        def claims = claimService.getClaimsByCustomerName(orgId, 'Correctlastname')

        then:
        1 * claimRepository.getClaims('test.no', _) >> [createPayment('123', 'Correctlastname')]
        claims.size() == 1
        claims.get(0).customer.name == 'Correctlastname'
    }

    def "Get payment given valid ordernumber returns list with payments matching given ordernumber"() {
        when:
        def claims = claimService.getClaimsByOrderNumber(orgId, '5')

        then:
        1 * claimRepository.getClaims('test.no', _ as Query) >> [createPayment('124', 'Testesen')]
        claims.size() == 1
        claims.get(0).orderNumber == '124'
    }

    def "Save payment given valid data returns void"() {
        given:
        def orderLine = new OrderLine(
                itemUri: '/varelinje/123'.toURI(),
                numberOfItems: 1,
                description: 'test'
        )
        def customer = new Customer(name: 'Testesen')
        def order = new Order(principalUri: 'link.to.Oppdragsgiver'.toURI(), orderLines: [orderLine], customers: [customer], requestedNumberOfDaysToPaymentDeadline: 7L)

        when:
        def response = claimService.setClaim(orgId, order)

        then:
        1 * claimFactory.createClaim(_ as Claim, 'test.no') >> [createPayment('123','Testesen')]
        1 * claimRepository.setClaim('test.no', _ as Claim)
        response.size() == 1
        response.get(0).orderLines.size() == 1
        response.get(0).orderNumber == '123'
        response.get(0).customer.name == 'Testesen'
    }

    private static Claim createPayment(String orderNumber, String name) {
        def varelinjeResource = new VarelinjeResource()
        varelinjeResource.setEnhet('unit')
        varelinjeResource.setKontering(new KontostrengResource())
        varelinjeResource.setPris(1L)
        varelinjeResource.addLink('self', new Link('link.to.VarelinjeResource'))
        def orderLine = new OrderLine(numberOfItems: 1L, itemUri: 'link.to.VarelinjeResource'.toURI(), itemPrice: 1L, description: 'test')
        def customer = new Customer(name: name, person: 'link.to.PersonResource'.toURI())

        return new Claim(
                orderLines: [orderLine],
                customer: customer,
                orderNumber: orderNumber,
                principalUri: 'link.to.Oppdragsgiver'.toURI(),
                requestedNumberOfDaysToPaymentDeadline: '7'
        )
    }
}
