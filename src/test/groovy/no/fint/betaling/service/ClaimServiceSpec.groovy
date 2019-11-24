package no.fint.betaling.service

import no.fint.betaling.factory.ClaimFactory
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Order
import no.fint.betaling.repository.ClaimRepository
import no.fint.betaling.util.BetalingObjectFactory
import no.fint.betaling.util.RestUtil
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class ClaimServiceSpec extends Specification {
    private ClaimService claimService
    private ClaimRepository claimRepository
    private ClaimFactory claimFactory
    private RestUtil restUtil
    private BetalingObjectFactory betalingObjectFactory

    void setup() {
        claimRepository = Mock()
        claimFactory = Mock()
        restUtil = Mock()
        claimService = new ClaimService(restUtil: restUtil, claimRepository: claimRepository, claimFactory: claimFactory)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "Send invoices given valid orgId sends invoices and updates payments"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.sendClaims(['12345'])

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        1 * restUtil.post(_ as Class<FakturagrunnlagResource>, _, _ as FakturagrunnlagResource) >> {
            ResponseEntity.ok().headers().location(new URI('link.to.Location')).build()
        }

        claims.size() == 1
        claims.get(0).claimStatus == ClaimStatus.SENT
        claims.get(0).invoiceUri == 'link.to.Location'.toURI()
    }

    def "Update invoice status given valid orgId updates payments"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)
        def invoice = betalingObjectFactory.newInvoice()

        when:
        claimService.updateClaimStatus()

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> invoice
        1 * claimRepository.updateClaim(_ as Query, _ as Update)
    }

    def "Set invoice given valid invoice returns valid response"() {
        given:
        def invoice = betalingObjectFactory.newInvoice()

        when:
        def response = claimService.submitClaim(invoice)

        then:
        1 * restUtil.post(_ as Class<FakturagrunnlagResource>, _, _ as FakturagrunnlagResource) >> {
            ResponseEntity.ok().headers().location(new URI('link.to.Location')).build()
        }

        response == 'link.to.Location'.toURI()
    }

    def "Get status given payment with valid location uri returns invoice"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)

        when:
        def invoice = claimService.getStatus(claim)

        then:
        1 * restUtil.get(_ as Class<FakturagrunnlagResource>, _) >> betalingObjectFactory.newInvoice()
        invoice.ordrenummer.identifikatorverdi == '12345'
    }

    def "Update invoice given valid invoice behaves as expected"() {
        given:
        def invoice = betalingObjectFactory.newInvoice()

        when:
        claimService.updateClaim(invoice)

        then:
        1 * claimRepository.updateClaim(_ as Query, _ as Update)
    }

    def "Get payments passes arguments to mongoservice"() {
        when:
        claimService.getClaims(new Query())

        then:
        1 * claimRepository.getClaims(_ as Query)
    }

    def "Update payment passes arguments to mongoservice"() {
        when:
        claimService.updateClaim(new Query(), new Update())

        then:
        1 * claimRepository.updateClaim(_ as Query, _ as Update)
    }

    def "Get all payments given valid orgId returns list"() {
        when:
        def claims = claimService.getAllClaims()

        then:
        1 * claimRepository.getClaims(_ as Query) >> [betalingObjectFactory.newClaim('12345', ClaimStatus.STORED), betalingObjectFactory.newClaim('12345', ClaimStatus.SENT)]
        claims.size() == 2
    }

    def "Get payment by name given valid lastname returns list with payments matching given lastname"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.getClaimsByCustomerName('Ola Testesen')

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        claims.size() == 1
        claims.get(0).customer.name == 'Ola Testesen'
    }

    def "Get payment given valid ordernumber returns list with payments matching given ordernumber"() {
        given:
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.getClaimsByOrderNumber('12')

        then:
        1 * claimRepository.getClaims(_ as Query) >> [claim]
        claims.size() == 1
        claims.get(0).orderNumber == '12345'
    }

    def "Save payment given valid data returns void"() {
        given:
        def order = betalingObjectFactory.newOrder()
        def claim = betalingObjectFactory.newClaim('12345', ClaimStatus.STORED)

        when:
        def claims = claimService.storeClaim(order)

        then:
        1 * claimFactory.createClaim(_ as Order) >> [claim]
        1 * claimRepository.storeClaim(_ as Claim)
        claims.size() == 1
        claims.every { it.orderItems.size() == 1 }
        claims.every { it.orderNumber == '12345' }
        claims.every { it.customer.name == 'Ola Testesen' }
    }
}
