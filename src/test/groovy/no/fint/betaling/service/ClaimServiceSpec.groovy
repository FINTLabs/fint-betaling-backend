package no.fint.betaling.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.factory.ClaimFactory
import no.fint.betaling.factory.InvoiceFactory
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Order
import no.fint.betaling.repository.ClaimRepository
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.betaling.util.RestUtil
import no.fint.model.administrasjon.okonomi.Faktura
import no.fint.model.resource.administrasjon.okonomi.FakturaResource
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.ResponseEntity
import spock.lang.Ignore
import spock.lang.Specification

class ClaimServiceSpec extends Specification {
    private ClaimService claimService
    private ClaimRepository claimRepository
    private ClaimFactory claimFactory
    private InvoiceFactory invoiceFactory
    private RestUtil restUtil
    private BetalingObjectFactory betalingObjectFactory
    private QueryService queryService

    void setup() {
        claimRepository = Mock()
        claimFactory = Mock()
        restUtil = Mock()
        invoiceFactory = Mock()
        queryService = new QueryService('mock.no')
        claimService = new ClaimService(
                restUtil: restUtil,
                claimRepository: claimRepository,
                claimFactory: claimFactory,
                invoiceFactory: invoiceFactory,
                queryService: queryService)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Given valid order, create and store claims"() {
        given:
        def order = betalingObjectFactory.newOrder()
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.storeClaims(order)

        then:
        1 * claimFactory.createClaims(_ as Order) >> [claim]
        1 * claimRepository.storeClaim(_ as Claim) >> claim
        claims.size() == 1
        claims.every { it.orderItems.size() == 1 }
        claims.every { it.orderNumber == '12345' }
        claims.every { it.customer.name == 'Ola Testesen' }
    }

    def "Given valid claims, send invoices and update claims"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.sendClaims(['12345'])

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        1 * restUtil.post(_, _ as FakturagrunnlagResource) >> new URI('link.to.Location')
        1 * invoiceFactory.createInvoice(claim) >> new FakturagrunnlagResource()
        claims.size() == 1
        claims.get(0).claimStatus == ClaimStatus.SENT
        claims.get(0).invoiceUri == 'link.to.Location'
    }

    @Ignore
    def "Send claim as inovice returns link to location"() {
        given:
        def invoice = betalingObjectFactory.newFakturagrunnlag()

        when:
        def response = claimService.sendClaim(invoice)

        then:
        1 * restUtil.post(_ as Class<FakturagrunnlagResource>, _) >> new URI('link.to.Location')
        response == 'link.to.Location'.toURI()
    }

    @Ignore
    def "Update claims fetches invoices and updates claims"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)
        def invoice = betalingObjectFactory.newFakturagrunnlag()

        when:
        claimService.updateClaims()

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> invoice
        1 * claimRepository.updateClaim(_ as Query, _ as Update)
    }

    @Ignore
    def "Get status given payment with valid location uri returns invoice"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)

        when:
        def invoice = claimService.getStatus(claim)

        then:
        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> betalingObjectFactory.newFakturagrunnlag()
        invoice.ordrenummer.identifikatorverdi == '12345'
    }

    def "Update claim given valid invoice updates claim"() {
        given:
        def invoice = betalingObjectFactory.newFakturagrunnlag()

        when:
        claimService.updateClaim(invoice)

        then:
        1 * claimRepository.updateClaim(_ as Query, _ as Update)
    }

    def "Get all claims returns list"() {
        when:
        def claims = claimService.getClaims()

        then:
        1 * claimRepository.getClaims(_ as Query) >> [betalingObjectFactory.newClaim('12345', ClaimStatus.STORED), betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)]
        claims.size() == 2
    }

    def "Get claims by customer name returns list of claims matching name"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.getClaimsByCustomerName('Ola Testesen')

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        claims.size() == 1
        claims.get(0).customer.name == 'Ola Testesen'
    }

    def "Get claims given valid order number returns list of claims matching order number"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.getClaimsByOrderNumber('12')

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        claims.size() == 1
        claims.get(0).orderNumber == '12345'
    }

    def 'Fetch links to Faktura for Fakturagrunnlag'() {
        given:
        def mapper = new ObjectMapper()
        def fakturagrunnlag = mapper.readValue(getClass().getResourceAsStream('/dummy_fakturagrunnlag.json'), FakturagrunnlagResource)

        when:
        claimService.updateClaim(fakturagrunnlag)

        then:
        2 * restUtil.get(FakturaResource, _) >>> [betalingObjectFactory.newFaktura(), betalingObjectFactory.newFaktura()]
        1 * claimRepository.updateClaim(_ as Query, {
            it.modifierOps['$set'].every { it.key in ['invoiceUri', 'invoiceNumbers', 'invoiceDate', 'paymentDueDate', 'amountDue'] }
        })
    }

}
