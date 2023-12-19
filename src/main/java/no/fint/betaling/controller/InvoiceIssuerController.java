package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Principal;
import no.fint.betaling.service.InvoiceIssuerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/principal")
public class InvoiceIssuerController {

    private final InvoiceIssuerService invoiceIssuerService;

    public InvoiceIssuerController(InvoiceIssuerService invoiceIssuerService) {
        this.invoiceIssuerService = invoiceIssuerService;
    }

    @GetMapping
    public ResponseEntity<Principal> getPrincipalForSchoolId(@RequestHeader(name = "x-school-org-id") String schoolId) {

        try {
            Principal principal = invoiceIssuerService.getInvoiceIssuer(schoolId);

            //Debug logging to solve product update issue
            principal.getLineitems().stream()
                    .filter(l -> l.getItemCode().contains("1351"))
                    .peek(l -> log.info("Return product: " + l.getItemCode() + " - " + l.getDescription()));

            return ResponseEntity.ok(principal);
        } catch (Exception ex) {
            log.error("An exception occured on handling getInvoiceIssuer", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
