package no.fint.betaling.invoiceissuer;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/principal")
public class InvoiceIssuerController {

    private final InvoiceIssuerService invoiceIssuerService;

    public InvoiceIssuerController(InvoiceIssuerService invoiceIssuerService) {
        this.invoiceIssuerService = invoiceIssuerService;
    }

    @GetMapping
    public Mono<Principal> getPrincipalForSchoolId(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return invoiceIssuerService.getInvoiceIssuer(schoolId)
                //Debug logging to solve product update issue
                .doOnNext(principal -> {
                    if (principal.getLineitems() != null) {
                        principal.getLineitems().stream()
                                .filter(l -> l.getItemCode().contains("1351"))
                                .peek(l -> log.info("Return product: " + l.getItemCode() + " - " + l.getDescription()));
                    }
                })
                .onErrorResume(ex -> {
                    log.error("An exception occured on handling getInvoiceIssuer", ex);
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
                });
    }
}
