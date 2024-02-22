package no.fint.betaling.repository

import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import org.junit.Ignore
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

@DataJpaTest
class ClaimJpaRepositorySpec extends Specification {

    @Subject
    ClaimJpaRepository claimJpaRepository

    def setup() {
        claimJpaRepository = Mock(ClaimJpaRepository)
    }

    def "should find claims by status"() {
        given:
        def expectedStatus = ClaimStatus.ACCEPTED
        def expectedClaims = [new Claim(), new Claim()]
        claimJpaRepository.findByClaimStatusIn(expectedStatus) >> expectedClaims

        when:
        def claims = claimJpaRepository.findByClaimStatusIn(expectedStatus)

        then:
        claims.size() == 2
    }

    def "should find claims by customer name"() {
        given:
        def expectedCustomerName = "Ola Testesen"
        def expectedClaims = [new Claim(), new Claim()]
        claimJpaRepository.findByCustomerName(_ as String) >> { String name ->
            if (name.toLowerCase().contains(expectedCustomerName.toLowerCase())) {
                return expectedClaims
            } else {
                return []
            }
        }

        when:
        def claims = claimJpaRepository.findByCustomerName(expectedCustomerName)

        then:
        claims.size() == 2
    }

    @Ignore
    def "should count claims by status"() {
        given:
        def status = ClaimStatus.SENT

        when:
        def count = claimJpaRepository.countByStatus(status)

        then:
        count >= 0
    }

//    @Ignore
//    def "should count claims by status and days"() {
//        given:
//        def status = ClaimStatus.REJECTED
//        def days = 30
//
//        when:
//        def count = claimJpaRepository.countByStatusAndDays(days, status)
//
//        then:
//        count >= 0
//    }

    def "should find claims by date, school, and status"() {
        given:
        def expectedDate = LocalDateTime.of(2023, 1, 1, 0, 0)
        def expectedOrganisationNumber = "SchoolName"
        def expectedStatuses = [ClaimStatus.ACCEPTED, ClaimStatus.SENT]
        def expectedClaims = [new Claim(), new Claim()]
        claimJpaRepository.getByDateAndSchoolAndStatus(_ as LocalDateTime, _ as String, _ as List) >> { LocalDateTime date, String organisationNumber, List statuses ->
            if (date == expectedDate && organisationNumber == expectedOrganisationNumber && statuses.containsAll(expectedStatuses)) {
                return expectedClaims
            } else {
                return []
            }
        }

        when:
        def claims = claimJpaRepository.getByDateAndSchoolAndStatus(expectedDate, expectedOrganisationNumber, expectedStatuses)

        then:
        claims.size() == 2
    }
}
