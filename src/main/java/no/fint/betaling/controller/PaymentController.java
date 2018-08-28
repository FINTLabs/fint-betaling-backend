package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Betaling;
import no.fint.betaling.model.Payment;
import no.fint.betaling.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @RequestMapping(method = POST)
    public ResponseEntity setPayment(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                     @RequestBody Payment payment) {
        log.info("{}: Received payment {}", orgId, payment);
        // TODO missing location header?
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.setPayment(orgId, payment));
    }

    @RequestMapping(method = GET)
    public List<Betaling> getAllPayments(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return paymentService.getAllPayments(orgId);
    }

    @GetMapping("/navn/{navn}")
    public List<Betaling> getPaymentByName(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                           @PathVariable(value = "navn") String name) {
        return paymentService.getPaymentsByCustomerName(orgId, name);
    }

    @GetMapping("/ordrenummer/{ordrenummer}")
    public List<Betaling> getPaymentByOrderNumber(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                  @PathVariable(value = "ordrenummer") String orderNumber) {
        return paymentService.getPaymentsByOrdernumber(orgId, orderNumber);
    }
}