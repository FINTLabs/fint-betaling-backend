package no.fint.betaling.service

import no.fint.betaling.claim.ClaimRestService
import no.fint.betaling.claim.ScheduleService
import no.fint.betaling.model.ClaimStatus
import spock.lang.Specification

import java.time.Duration

class ScheduleServiceSpec extends Specification {
    ClaimRestService claimRestService = Mock(ClaimRestService)
    ScheduleService scheduleService = new ScheduleService(claimRestService)

    void "updateAcceptedClaims() should call claimRestService.updateClaimsByStatusAndAge() with ClaimStatus.ACCEPTED and 30 days"() {
        when:
        scheduleService.updateAcceptedClaims()

        then:
        1 * claimRestService.updateClaimsByStatusAndAge(ClaimStatus.ACCEPTED, Duration.ofDays(30))
    }

    void "updateIssuedClaims() should call claimRestService.updateClaimsByStatusAndAge() with ClaimStatus.ISSUED and 100 days"() {
        when:
        scheduleService.updateIssuedClaims()

        then:
        1 * claimRestService.updateClaimsByStatusAndAge(ClaimStatus.ISSUED, Duration.ofDays(100))
    }

    void "updateUpdateErrorClaims() should call claimRestService.updateClaimsByStatusAndAge() with ClaimStatus.UPDATE_ERROR and 30 days"() {
        when:
        scheduleService.updateUpdateErrorClaims()

        then:
        1 * claimRestService.updateClaimsByStatusAndAge(ClaimStatus.UPDATE_ERROR, Duration.ofDays(30))
    }
}
