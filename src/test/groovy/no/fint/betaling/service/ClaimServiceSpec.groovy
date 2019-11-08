package no.fint.betaling.service

import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.model.OrderLine
import no.fint.betaling.repository.ClaimRepository
import no.fint.betaling.service.ClaimService
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class ClaimServiceSpec extends Specification {
    private ClaimService claimService
    private RestUtil restUtil
    private ClaimRepository claimRepository

    void setup() {
        restUtil = Mock(RestUtil) {
            get(_ as Class<FakturagrunnlagResource>, _ as URI, 'valid.org') >> createInvoice()
            post(_ as Class<FakturagrunnlagResource>, _ as URI, _ as FakturagrunnlagResource, _ as String) >> {
                ResponseEntity.ok().headers().location(new URI('http', 'valid.host', '/path', '')).build()
            }
        }
        claimRepository = Mock(ClaimRepository)
        claimService = new ClaimService(
                restUtil: restUtil,
                claimRepository: claimRepository,
                invoiceEndpoint: 'enpoints/invoice'.toURI()
        )
    }

    def "Send invoices given valid orgId sends invoices and updates payments"() {
        when:
        claimService.sendClaims('valid.org')

        then:
        1 * claimRepository.getClaims(_ as String, _ as Query) >> [createClaim(false)]
        1 * claimRepository.updateClaim('valid.org', _ as Query, _ as Update)
    }

    def "Update invoice status given valid orgId updates payments"() {
        when:
        claimService.updateClaimStatus('valid.org')

        then:
        1 * claimRepository.getClaims(_ as String, _ as Query) >> [createClaim(true)]
        1 * claimRepository.updateClaim(_ as String, _ as Query, _ as Update)
    }


    def "Set invoice given valid invoice returns valid response"() {
        when:
        def response = claimService.submitClaim('valid.org', createInvoice())

        then:
        response.getStatusCode().is2xxSuccessful()
        response.getHeaders().getLocation().getHost() == 'valid.host'
    }

    def "Get status given payment with valid location uri returns invoice"() {
        when:
        def invoice = claimService.getStatus(
                'valid.org',
                new Claim(location: new URI('http', 'valid.location', '/path', ''))
        )

        then:
        invoice.ordrenummer.identifikatorverdi == '1234'
    }

    def "Update invoice given valid invoice behaves as expected"() {
        when:
        claimService.updateClaim('valid.org', createInvoice())

        then:
        1 * claimRepository.updateClaim(_ as String, _ as Query, _ as Update)
    }

    def "Get payments passes arguments to mongoservice"() {
        when:
        claimService.getClaims('valid.org', new Query())

        then:
        1 * claimRepository.getClaims('valid.org', _ as Query)
    }

    def "Update payment passes arguments to mongoservice"() {
        when:
        claimService.updateClaim('valid.org', new Query(), new Update())

        then:
        1 * claimRepository.updateClaim('valid.org', _ as Query, _ as Update)
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

    private static FakturagrunnlagResource createInvoice() {
        def resource = new FakturagrunnlagResource(
                ordrenummer: new Identifikator(identifikatorverdi: '1234'),
                fakturalinjer: [
                        new FakturalinjeResource(
                                antall: 1,
                                fritekst: ['testLine'],
                                pris: 1000L
                        )
                ]
        )
        resource.addLink("self", Link.with("/some/path/to/self"))
        return resource
    }

    private static Claim createClaim(boolean sent) {
        def claim = new Claim()
        claim.invoiceUri = 'link.to.FakturagrunnlagResource'.toURI()
        claim.claimStatus = ClaimStatus.STORED
        return claim
    }
}
