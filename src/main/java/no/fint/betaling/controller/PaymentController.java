package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.model.Payment;
import no.fint.betaling.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @RequestMapping(method = POST)
    public ResponseEntity setPayment(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId,
                                     @RequestBody Payment payment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.setPayment(orgId, payment));
    }

    @RequestMapping(method = GET)
    public ResponseEntity getAllPayments(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        return ResponseEntity.ok(paymentService.getAllPayments(orgId));
    }

    @GetMapping("/navn/{navn}")
    public ResponseEntity getPaymentByName(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId,
                                           @PathVariable(value = "navn") String name) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerName(orgId, name));
    }

    @GetMapping("/ordrenummer/{ordrenummer}")
    public ResponseEntity getPaymentByOrderNumber(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId,
                                                  @PathVariable(value = "ordrenummer") String orderNumber) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrdernumber(orgId, orderNumber));
    }
}