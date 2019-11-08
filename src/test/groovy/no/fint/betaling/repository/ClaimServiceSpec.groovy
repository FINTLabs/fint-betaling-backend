package no.fint.betaling.repository

import no.fint.betaling.model.Betaling
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
    private ClaimService invoiceRepository
    private RestUtil restUtil
    private ClaimRepository mongoService

    void setup() {
        restUtil = Mock(RestUtil) {
            get(_ as Class<FakturagrunnlagResource>, _ as String, 'valid.org') >> createInvoice()
            post(_ as Class<FakturagrunnlagResource>, _ as String, _ as FakturagrunnlagResource, _ as String) >> {
                ResponseEntity.ok().headers().location(new URI('http', 'valid.host', '/path', '')).build()
            }
        }
        mongoService = Mock(ClaimRepository)
        invoiceRepository = new ClaimService(
                restUtil: restUtil,
                claimRepository: mongoService,
                invoiceEndpoint: 'enpoints/invoice'
        )
    }

    def "Send invoices given valid orgId sends invoices and updates payments"() {
        when:
        invoiceRepository.sendClaims('valid.org')

        then:
        1 * mongoService.getClaims(_ as String, _ as Query) >> [createPayment(false)]
        1 * mongoService.updateClaim('valid.org', _ as Query, _ as Update)
    }

    def "Update invoice status given valid orgId updates payments"() {
        when:
        invoiceRepository.updateClaimStatus('valid.org')

        then:
        1 * mongoService.getClaims(_ as String, _ as Query) >> [createPayment(true)]
        1 * mongoService.updateClaim(_ as String, _ as Query, _ as Update)
    }


    def "Set invoice given valid invoice returns valid response"() {
        when:
        def response = invoiceRepository.submitClaim('valid.org', createInvoice())

        then:
        response.getStatusCode().is2xxSuccessful()
        response.getHeaders().getLocation().getHost() == 'valid.host'
    }

    def "Get status given payment with valid location uri returns invoice"() {
        when:
        def invoice = invoiceRepository.getStatus(
                'valid.org',
                new Betaling(location: new URI('http', 'valid.location', '/path', ''))
        )

        then:
        invoice.ordrenummer.identifikatorverdi == '1234'
    }

    def "Update invoice given valid invoice behaves as expected"() {
        when:
        invoiceRepository.updateClaim('valid.org', createInvoice())

        then:
        1 * mongoService.updateClaim(_ as String, _ as Query, _ as Update)
    }

    def "Get payments passes arguments to mongoservice"() {
        when:
        invoiceRepository.getClaims('valid.org', new Query())

        then:
        1 * mongoService.getClaims('valid.org', _ as Query)
    }

    def "Update payment passes arguments to mongoservice"() {
        when:
        invoiceRepository.updateClaim('valid.org', new Query(), new Update())

        then:
        1 * mongoService.updateClaim('valid.org', _ as Query, _ as Update)
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

    private static Betaling createPayment(boolean sent) {
        def payment = new Betaling()
        payment.setFakturagrunnlag(createInvoice())
        payment.location = new URI('http', 'host.test', '/location', '').toString()
        payment.sentTilEksterntSystem = sent
        return payment
    }
}
