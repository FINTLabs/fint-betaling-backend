package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/test")
public class TestScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/check")
    public ResponseEntity checkInvoice(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId){
        scheduleService.checkInvoiceStatus(orgId);
        return ResponseEntity.ok("sent invoices");
    }

    @GetMapping("/send")
    public ResponseEntity sendInvoice(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId){
        scheduleService.sendInvoices(orgId);
        return ResponseEntity.ok("sent invoices");
    }
}
