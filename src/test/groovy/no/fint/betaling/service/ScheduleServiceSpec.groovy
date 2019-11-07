package no.fint.betaling.service


import spock.lang.Specification

class ScheduleServiceSpec extends Specification {
    private ClaimService invoiceRepository
    private ScheduleService scheduleService

    void setup() {
        invoiceRepository = Mock(ClaimService)
        scheduleService = new ScheduleService(invoiceRepository: invoiceRepository)
    }

    def "Send invoices"() {
        when:
        scheduleService.sendInvoices()

        then:
        invoiceRepository.sendClaims(_ as String)
    }
}
