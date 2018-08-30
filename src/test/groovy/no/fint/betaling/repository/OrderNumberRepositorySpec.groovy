package no.fint.betaling.repository

import no.fint.betaling.model.OrgConfig
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class OrderNumberRepositorySpec extends Specification {

    private MongoTemplate mongoTemplate
    private OrderNumberRepository ordernumberService

    void setup() {
        mongoTemplate = Mock(MongoTemplate)
        ordernumberService = new OrderNumberRepository(mongoTemplate: mongoTemplate)
    }

    def "Get ordernumber given existing orgId returns valid ordernumber"() {
        when:
        def ordernumber = ordernumberService.getOrderNumber('existing.org')

        then:
        1 * mongoTemplate.findAndModify(_, _, _, _) >> new OrgConfig(orgId: 'existing.org', nesteOrdrenummer: 1001)
        ordernumber == 1001
    }

    def "Get ordernumber given nonexisting orgId returns valid ordernumber"() {
        when:
        def ordernumber = ordernumberService.getOrderNumber('notexisting.org')

        then:
        1 * mongoTemplate.findAndModify(_, _, _, _) >> null
        1 * mongoTemplate.save(_, _)
        1 * mongoTemplate.count(_, _) >> 1
        1 * mongoTemplate.findAndModify(_, _, _, _) >> new OrgConfig(nesteOrdrenummer: 0)
        ordernumber == 0
    }

}
