package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/update")
    public ResponseEntity updateInvoices(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        log.info("Update Invoice Status for {}", orgId);
        invoiceService.updateInvoiceStatus(orgId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/send")
    public ResponseEntity sendInvoices(@RequestHeader(name = ORG_ID, defaultValue = HeaderConstants.DEFAULT_VALUE_ORG_ID) String orgId) {
        log.info("Send Invoices for {}", orgId);
        invoiceService.sendInvoices(orgId);
        return ResponseEntity.noContent().build();
    }
}
