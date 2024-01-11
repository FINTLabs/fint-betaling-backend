package no.fint.betaling.repository

import no.fint.betaling.model.ClaimStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDate

@Ignore("Not implemented yet")
@DataJpaTest
class ClaimJpaRepositorySpec extends Specification {

    def setupSpec() {
        claimJpaRepository = Mock(ClaimJpaRepository)
    }

    def "should find highest order number"() {
        when:
        def highestOrderNumber = claimJpaRepository.findHighestOrderNumber()

        then:
        highestOrderNumber.isPresent()
    }

    def "should find claims by status"() {
        given:
        def status = ClaimStatus.ACCEPTED

        when:
        def claims = claimJpaRepository.findByClaimStatusIn(status)

        then:
        claims.size() > 0
    }

    def "should find claims by customer name"() {
        given:
        def customerName = "John Doe"

        when:
        def claims = claimJpaRepository.findByCustomerName(customerName)

        then:
        claims.size() > 0
    }

    def "should count claims by status"() {
        given:
        def status = ClaimStatus.SENT

        when:
        def count = claimJpaRepository.countByStatus(status)

        then:
        count >= 0
    }

    def "should count claims by status and days"() {
        given:
        def status = ClaimStatus.REJECTED
        def days = 30

        when:
        def count = claimJpaRepository.countByStatusAndDays(days, status)

        then:
        count >= 0
    }

    def "should find claims by date, school, and status"() {
        given:
        def date = LocalDate.now()
        def school = "SchoolName"
        def status = [ClaimStatus.ACCEPTED, ClaimStatus.SENT]

        when:
        def claims = claimJpaRepository.getByDateAndSchoolAndStatus(date, school, status)

        then:
        claims.size() >= 0
    }
}
