package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Betaling;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private InvoiceService invoiceService;

    @Value("${fint.betaling.default-org-id}")
    private String orgId;

    @Scheduled(fixedRateString = "${fint.betaling.invoice-send-rate}")
    public void sendInvoices(){
        invoiceService.sendInvoices(orgId);
    }
}
