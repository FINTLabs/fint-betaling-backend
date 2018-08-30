package no.fint.betaling.controller;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;


    @GetMapping
    public List<Kunde> getCustomers(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                    @RequestParam(value = "navn", required = false) String name) {
        return customerService.getCustomers(orgId, name);
    }
}
