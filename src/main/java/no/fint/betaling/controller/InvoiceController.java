package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Payment;
import no.fint.betaling.service.InvoiceService;
import no.fint.betaling.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/betaling")
public class InvoiceController {

    @Autowired
    private MongoService mongoService;

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/save")
    public ResponseEntity setFakturagrunnlag(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                             @RequestBody Payment payment) {
        return ResponseEntity.ok(mongoService.saveFakturagrunnlag(orgId, payment.getFakturagrunnlag(), payment.getKunde()));
    }

    @GetMapping
    public ResponseEntity getAllInvoices(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(orgId));
    }

    @GetMapping("/navn")
    public ResponseEntity getInvoiceByName(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                           @RequestParam(value = "lastname") String lastName) {
        return ResponseEntity.ok(invoiceService.getInvoiceByLastname(orgId, lastName));
    }

    @GetMapping("/ordrenummer")
    public ResponseEntity getInvoiceByOrderNumber(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                                  @RequestParam(value = "query") String ordernumber) {
        return ResponseEntity.ok(invoiceService.getInvoiceByOrderNumber(orgId, ordernumber));

    }
}
