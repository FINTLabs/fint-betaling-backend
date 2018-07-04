package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Payment;
import no.fint.betaling.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/save")
    public ResponseEntity setPayment(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                     @RequestBody Payment payment) {
        paymentService.setPayment(orgId, payment.getOrderLines(), payment.getCustomers());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity getAllPayments(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId) {
        return ResponseEntity.ok(paymentService.getAllPayments(orgId));
    }

    @GetMapping("/navn/{etternavn}")
    public ResponseEntity getPaymentByName(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                           @PathVariable(value = "etternavn") String lastName) {
        return ResponseEntity.ok(paymentService.getPaymentsByLastname(orgId, lastName));
    }

    @GetMapping("/ordrenummer/{ordrenummer}")
    public ResponseEntity getPaymentByOrderNumber(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId,
                                                  @PathVariable(value = "ordrenummer") String orderNumber) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrdernumber(orgId, orderNumber));
    }
}