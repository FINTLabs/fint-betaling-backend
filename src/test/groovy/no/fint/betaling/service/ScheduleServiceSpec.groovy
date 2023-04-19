package no.fint.betaling.service

import spock.lang.Ignore
import spock.lang.Specification

class ScheduleServiceSpec extends Specification {
    ClaimService claimService = Mock(ClaimService)

    ScheduleService scheduleService = new ScheduleService(claimService)

    void "updateRecentlySentClaims() should call claimService.updateSentClaims()"() {
        given:
        claimService.updateSentClaims() >> { println "Updating sent claims..." }

        when:
        scheduleService.updateRecentlySentClaims()

        then:
        1 * claimService.updateSentClaims()
    }

    void "updateAcceptedClaims() should call claimService.updateAcceptedClaims()"() {
        given:
        claimService.updateAcceptedClaims() >> { println "Updating accepted claims..." }

        when:
        scheduleService.updateAcceptedClaims()

        then:
        1 * claimService.updateAcceptedClaims()
    }
}
