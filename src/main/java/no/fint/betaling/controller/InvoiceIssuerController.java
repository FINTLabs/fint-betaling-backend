package no.fint.betaling.controller;

import no.fint.betaling.model.Principal;
import no.fint.betaling.service.InvoiceIssuerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/principal")
public class InvoiceIssuerController {

    private final InvoiceIssuerService invoiceIssuerService;

    public InvoiceIssuerController(InvoiceIssuerService invoiceIssuerService) {
        this.invoiceIssuerService = invoiceIssuerService;
    }

    @GetMapping
    public Principal getPrincipalForSchoolId(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return invoiceIssuerService.getInvoiceIssuer(schoolId);
    }
}
