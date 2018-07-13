package no.fint.betaling.service

import spock.lang.Specification

class ScheduleServiceSpec extends Specification {
    private InvoiceService invoiceService
    private ScheduleService scheduleService

    void setup() {
        invoiceService = Mock(InvoiceService)
        scheduleService = new ScheduleService(invoiceService: invoiceService)
    }

    def "Send invoices"() {
        when:
        scheduleService.sendInvoices()

        then:
        invoiceService.sendInvoices(_ as String)
    }
}
