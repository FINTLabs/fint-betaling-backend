package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Customer;
import no.fint.betaling.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/customer")
public class CustomerController {

    /*
    To be deleted???
     */

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> getCustomers(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                       @RequestParam(value = "name", required = false) String name) {
        return customerService.getCustomers(orgId, name);
    }
}
