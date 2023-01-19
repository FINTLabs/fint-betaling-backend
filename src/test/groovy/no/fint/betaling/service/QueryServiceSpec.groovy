package no.fint.betaling.service


import org.springframework.data.mongodb.core.query.Query
import spock.lang.Ignore
import spock.lang.Specification

@Ignore("Work in progress")
class QueryServiceSpec extends Specification {
    private QueryService queryService
    private Query query
    private String orgId = "some_value"

    def setup() {
        queryService = Mock(QueryService, constructorArgs: [orgId]) as QueryService
    }


    def "createQuery method should return a query with the correct criteria"() {
        when:
        query = queryService.createQuery()

        then:
        query.toString().contains("Criteria.where(\"_class\").is(Claim.class.getName())")
        query.toString().contains("Criteria.where(\"orgId\").is(orgId)")
    }


    def "createQuery method should return a query"() {
        when:
        query = queryService.createQuery()

        then:
        query instanceof Query
    }
}


