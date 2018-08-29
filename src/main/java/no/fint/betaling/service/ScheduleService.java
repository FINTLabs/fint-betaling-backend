package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Value("${fint.betaling.default-org-id}")
    private String orgId;

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.invoice-send-rate}")
    public void sendInvoices(){
        log.info("Sending invoices...");
        try {
            invoiceRepository.sendInvoices(orgId);
        } catch (Exception e) {
            log.error("Error caught when sending invoices!", e);
        }
    }


    @Scheduled(initialDelay = 20000, fixedRateString = "${fint.betaling.invoice-update-rate}")
    public void updateInvoices(){
        log.info("Updating invoices...");
        try {
            invoiceRepository.updateInvoiceStatus(orgId);
        } catch (Exception e) {
            log.error("Error caught when updating invoices!", e);
        }
    }
}
