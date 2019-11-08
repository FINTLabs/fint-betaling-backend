package no.fint.betaling.repository

import no.fint.betaling.config.OrganisationConfig
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class OrderNumberRepositorySpec extends Specification {

    private MongoTemplate mongoTemplate
    private OrderNumberRepository orderNumberRepository

    void setup() {
        mongoTemplate = Mock(MongoTemplate)
        orderNumberRepository = new OrderNumberRepository(mongoTemplate: mongoTemplate)
    }

    def "Get order number given existing orgId returns valid order number"() {
        when:
        def orderNumber = orderNumberRepository.getOrderNumber('existing.org')

        then:
        1 * mongoTemplate.findAndModify(_, _, _, _) >> new OrganisationConfig(orgId: 'existing.org', nextOrderNumberForOrganisation: 1001L)
        orderNumber == '1001'
    }

    def "Get order number given non-existing orgId returns valid order number"() {
        when:
        def orderNumber = orderNumberRepository.getOrderNumber('notexisting.org')

        then:
        1 * mongoTemplate.findAndModify(_, _, _, _) >> null
        1 * mongoTemplate.save(_, _)
        1 * mongoTemplate.count(_, _) >> 1
        1 * mongoTemplate.findAndModify(_, _, _, _) >> new OrganisationConfig(nextOrderNumberForOrganisation: 0L)
        orderNumber == '0'
    }
}
