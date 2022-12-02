package no.fint.betaling.controller;

import no.fint.betaling.config.ApplicationProperties;
import no.fint.betaling.model.Principal;
import no.fint.betaling.service.InvoiceIssuerService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/principal")
public class InvoiceIssuerController {

    private final InvoiceIssuerService invoiceIssuerService;

    private ApplicationProperties applicationProperties;

    public InvoiceIssuerController(InvoiceIssuerService invoiceIssuerService, ApplicationProperties applicationProperties) {
        this.invoiceIssuerService = invoiceIssuerService;
        this.applicationProperties = applicationProperties;
    }

    @GetMapping
//    public Principal getPrincipalForSchoolId(@RequestHeader(name = "x-school-org-id") String schoolId,
//                                             @RequestHeader(name = "x-feide-upn") String feideUpn) {
    public Principal getPrincipalForSchoolId(@AuthenticationPrincipal Jwt jwt) {

        String orgnizationNumber;
        String employeeId;


        if (applicationProperties.getDemo()) {
            orgnizationNumber = applicationProperties.getDemoUserOrgId();
            employeeId = applicationProperties.getDemoUserEmployeeId();
        } else {
            FintJwtEndUserPrincipal endUserPrincipal = FintJwtEndUserPrincipal.from(jwt);
            orgnizationNumber = endUserPrincipal.getOrganizationNumber();
            employeeId = endUserPrincipal.getEmployeeId();
        }

        return invoiceIssuerService.getPrincipalByOrganisationId(orgnizationNumber, employeeId);
    }
}
