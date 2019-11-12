package no.fint.betaling.service

import spock.lang.Ignore
import spock.lang.Specification

class ScheduleServiceSpec extends Specification {
    private ClaimService claimService
    private ScheduleService scheduleService

    void setup() {
        claimService = Mock(ClaimService)
        scheduleService = new ScheduleService(claimService: claimService)
    }

    @Ignore
    def "Send invoices"() {
        when:
        scheduleService.sendInvoices()

        then:
        claimService.sendClaims(_ as String)
    }
}
