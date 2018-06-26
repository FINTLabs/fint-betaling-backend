package no.fint.betaling.service

import no.fint.betaling.model.OrgConfig
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class OrderNumberServiceSpec extends Specification {

    private MongoTemplate mongoTemplate
    private OrderNumberService ordernumberService

    void setup() {
        mongoTemplate = Mock(MongoTemplate)
        ordernumberService = new OrderNumberService(mongoTemplate: mongoTemplate)
    }

    def "Get ordernumber given existing orgId returns valid ordernumber"() {
        when:
        def ordernumber = ordernumberService.getOrderNumber('existing.org')

        then:
        1 * mongoTemplate.findOne(_, _, _) >> new OrgConfig(orgId: 'existing.org', sisteOrdrenummer: 1000)
        1 * mongoTemplate.updateFirst(_, _, _)
        ordernumber == 'existingorg1001'
    }

    def "Get ordernumber given nonexisting orgId returns valid ordernumber"() {
        when:
        def ordernumber = ordernumberService.getOrderNumber('notexisting.org')

        then:
        1 * mongoTemplate.findOne(_, _, _) >> null
        1 * mongoTemplate.save(_,_)
        ordernumber == 'notexistingorg0'
    }

    def "Get ordernumber from number given number and orgId returns valid ordernumber"(){
        when:
        def ordernumber = ordernumberService.getOrderNumberFromNumber('valid.org', '5')

        then:
        ordernumber == 'validorg5'
    }
}
