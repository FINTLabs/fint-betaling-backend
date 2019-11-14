package no.fint.betaling.repository

import no.fint.betaling.config.OrganisationConfig
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.util.BetalingObjectFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import spock.lang.Specification

class OrderNumberRepositorySpec extends Specification {

    private MongoTemplate mongoTemplate
    private OrderNumberRepository orderNumberRepository
    private BetalingObjectFactory betalingObjectFactory

    void setup() {
        mongoTemplate = Mock(MongoTemplate)
        orderNumberRepository = new OrderNumberRepository(mongoTemplate: mongoTemplate)
        betalingObjectFactory = new BetalingObjectFactory();
    }

    def "Get order number given existing orgId returns valid order number"() {
        when:
        def orderNumber = orderNumberRepository.getOrderNumber()

        then:
        1 * mongoTemplate.findAndModify(_ as Query , _ as Update, _ as Class<OrganisationConfig>, _) >> new OrganisationConfig(orgId: 'existing.org', nextOrderNumberForOrganisation: 1001L)
        orderNumber == '1001'
    }

    def "Get highest order number"() {
        given:
        def lowClaim = betalingObjectFactory.newClaim('1234', ClaimStatus.SENT)
        def highClaim = betalingObjectFactory.newClaim('5678', ClaimStatus.STORED)

        when:
        def orderNumber = orderNumberRepository.getHighestOrderNumber()

        then:
        1 * mongoTemplate.find(_ as Query, _ as Class<Claim>) >> [lowClaim, highClaim]
        orderNumber == 5678
    }
}
