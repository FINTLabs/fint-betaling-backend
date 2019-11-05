package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.Order;
import no.fint.betaling.repository.PaymentRepository;
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
    private PaymentRepository paymentRepository;

    @RequestMapping(method = POST)
    public ResponseEntity setPayment(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                     @RequestBody Order order) {
        log.info("{}: Received payment {}", orgId, order);
        // TODO missing location header?
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentRepository.setPayment(orgId, order));
    }

    @RequestMapping(method = GET)
    public List<Claim> getAllPayments(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return paymentRepository.getAllPayments(orgId);
    }

    @GetMapping("/navn/{navn}")
    public List<Claim> getPaymentByName(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                        @PathVariable(value = "navn") String name) {
        return paymentRepository.getPaymentsByCustomerName(orgId, name);
    }

    @GetMapping("/ordrenummer/{ordrenummer}")
    public List<Claim> getPaymentByOrderNumber(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                               @PathVariable(value = "ordrenummer") String orderNumber) {
        return paymentRepository.getPaymentsByOrdernumber(orgId, orderNumber);
    }
}