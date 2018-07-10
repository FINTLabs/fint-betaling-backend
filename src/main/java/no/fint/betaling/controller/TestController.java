package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.MongoService;
import no.fint.betaling.service.OrderNumberService;
import no.fint.betaling.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/test")
public class TestController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/check")
    public ResponseEntity checkInvoice(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        scheduleService.checkInvoiceStatus(orgId);
        return ResponseEntity.ok("checked invoices");
    }

    @GetMapping("/send")
    public ResponseEntity sendInvoice(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        scheduleService.sendInvoices(orgId);
        return ResponseEntity.ok("sent invoices");
    }
}