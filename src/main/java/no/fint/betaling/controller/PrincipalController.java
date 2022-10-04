package no.fint.betaling.controller;

import no.fint.betaling.config.ApplicationProperties;
import no.fint.betaling.model.Principal;
import no.fint.betaling.service.PrincipalService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/principal")
public class PrincipalController {

    private final PrincipalService principalService;

    private ApplicationProperties applicationProperties;

    public PrincipalController(PrincipalService principalService, ApplicationProperties applicationProperties) {
        this.principalService = principalService;
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

        return principalService.getPrincipalByOrganisationId(orgnizationNumber, employeeId);
    }
}
