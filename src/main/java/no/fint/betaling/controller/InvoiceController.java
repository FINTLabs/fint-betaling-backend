package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/update")
    public ResponseEntity updateInvoices(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        invoiceService.updateInvoiceStatus(orgId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/send")
    public ResponseEntity sendInvoices(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        invoiceService.sendInvoices(orgId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
