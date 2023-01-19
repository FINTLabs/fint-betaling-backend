package no.fint.betaling.service


import spock.lang.Specification

class ScheduleServiceSpec extends Specification {

    def "Send invoices"() {
        given:
        def claimService = Mock(ClaimService)
        def scheduleService = new ScheduleService(claimService)

        when:
        scheduleService.updateClaims()

        then:
        1 * claimService.updateClaims()
    }

}
