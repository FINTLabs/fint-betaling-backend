package no.fint.betaling.service


import no.fint.betaling.claim.ClaimRestService
import no.fint.betaling.claim.ScheduleService
import spock.lang.Specification

class ScheduleServiceSpec extends Specification {
    ClaimRestService claimRestService = Mock(ClaimRestService)

    ScheduleService scheduleService = new ScheduleService(claimRestService)

    void "updateRecentlySentClaims() should call claimService.updateSentClaims()"() {
        given:
        claimRestService.updateSentClaims() >> { println "Updating sent claims..." }

        when:
        scheduleService.updateRecentlySentClaims()

        then:
        1 * claimRestService.updateSentClaims()
    }

    void "updateAcceptedClaims() should call claimService.updateAcceptedClaims()"() {
        given:
        claimRestService.updateAcceptedClaims() >> { println "Updating accepted claims..." }

        when:
        scheduleService.updateAcceptedClaims()

        then:
        1 * claimRestService.updateAcceptedClaims()
    }
}
