package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Payment;
import no.fint.betaling.service.PaymentService;
import no.fint.betaling.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/save")
    public ResponseEntity setPayment(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                             @RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.setPayment(orgId, payment.getFakturagrunnlag(), payment.getKunde()));
    }

    @GetMapping
    public ResponseEntity getAllPayments(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId) {
        return ResponseEntity.ok(paymentService.getAllPayments(orgId));
    }

    @GetMapping("/navn")
    public ResponseEntity getPaymentByName(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                           @RequestParam(value = "etternavn") String lastName) {
        return ResponseEntity.ok(paymentService.getPaymentsByLastname(orgId, lastName));
    }

    @GetMapping("/ordrenummer")
    public ResponseEntity getPaymentByOrdernumber(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                                  @RequestParam(value = "ordrenummer") String ordernumber) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrdernumber(orgId, ordernumber));
    }
}
