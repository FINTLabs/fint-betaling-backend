package no.fint.betaling.service

import no.fint.betaling.model.OrgConfig
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class OrdernumberServiceSpec extends Specification {

    private MongoTemplate mongoTemplate
    private OrdernumberService ordernumberService

    void setup() {
        mongoTemplate = Mock(MongoTemplate)
        ordernumberService = new OrdernumberService(mongoTemplate: mongoTemplate)
    }

    def "Get ordernumber given existing orgId returns valid ordernumber"() {
        when:
        def ordernumber = ordernumberService.getOrdernumber('existing.org')

        then:
        1 * mongoTemplate.find(_, _, _) >> [new OrgConfig(orgId: 'existing.org', sisteOrdrenummer: 1000)]
        1 * mongoTemplate.updateFirst(_, _, _)
        ordernumber == 'existingorg1001'
    }

    def "Get ordernumber given nonexisting orgId returns valid ordernumber"() {
        when:
        def ordernumber = ordernumberService.getOrdernumber('notexisting.org')

        then:
        1 * mongoTemplate.find(_, _, _) >> []
        1 * mongoTemplate.save(_,_)
        ordernumber == 'notexistingorg0'
    }
}
