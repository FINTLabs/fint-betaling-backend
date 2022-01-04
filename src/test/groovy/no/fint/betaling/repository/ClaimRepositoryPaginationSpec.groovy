package no.fint.betaling.repository

import no.fint.betaling.model.Claim
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

import java.util.stream.IntStream

@DataMongoTest
class ClaimRepositoryPaginationSpec extends Specification {

    private ClaimRepository claimRepository

    @Autowired
    private MongoTemplate mongoTemplate

    void setup() {
        this.claimRepository = new ClaimRepository(mongoTemplate: mongoTemplate, orgId: "fake.no")
        IntStream.range(1, 101).forEach(
                i -> {
                    def newClaim = new Claim()
                    newClaim.setStatusMessage(i.toString())
                    claimRepository.storeClaim(newClaim)
                }
        )
    }

    void cleanup() {
        mongoTemplate.dropCollection(Claim.class.simpleName.toLowerCase())
    }

    def "Check the amount of total pages"(int pageSize, int totalPages) {
        when:
        def result = claimRepository.getClaimsWithPagination(new Query(), PageRequest.of(1, pageSize))

        then:
        result.totalElements == 100
        result.getPageable().getPageSize() == pageSize
        result.totalPages == totalPages

        where:
        pageSize | totalPages
        10       | 10
        20       | 5
        5        | 20
        50       | 2
        1        | 100
        100      | 1
    }

    def "The page should contain the rigth elements"(int pageIndex, String first, String last) {
        when:
        def result = claimRepository.getClaimsWithPagination(new Query(), PageRequest.of(pageIndex, 10))

        then:
        result.toList().first().statusMessage == first
        result.toList().last().statusMessage == last

        where:
        pageIndex | first | last
        0         | 1     | 10
        1         | 11    | 20
        2         | 21    | 30
        9         | 91    | 100
    }
}
