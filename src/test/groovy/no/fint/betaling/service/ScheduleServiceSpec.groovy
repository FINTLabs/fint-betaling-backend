package no.fint.betaling.service

import no.fint.betaling.repository.InvoiceRepository
import spock.lang.Specification

class ScheduleServiceSpec extends Specification {
    private InvoiceRepository invoiceRepository
    private ScheduleService scheduleService

    void setup() {
        invoiceRepository = Mock(InvoiceRepository)
        scheduleService = new ScheduleService(invoiceRepository: invoiceRepository)
    }

    def "Send invoices"() {
        when:
        scheduleService.sendInvoices()

        then:
        invoiceRepository.sendInvoices(_ as String)
    }
}
