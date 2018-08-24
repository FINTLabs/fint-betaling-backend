package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/update")
    public ResponseEntity updateInvoices(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        log.info("Update Invoice Status for {}", orgId);
        invoiceService.updateInvoiceStatus(orgId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/send")
    public ResponseEntity sendInvoices(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        log.info("Send Invoices for {}", orgId);
        invoiceService.sendInvoices(orgId);
        return ResponseEntity.noContent().build();
    }
}
