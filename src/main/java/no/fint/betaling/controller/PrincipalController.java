package no.fint.betaling.controller;

import no.fint.betaling.config.ApplicationProperties;
import no.fint.betaling.model.Principal;
import no.fint.betaling.service.PrincipalService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.apache.commons.lang3.StringUtils;
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

        FintJwtEndUserPrincipal endUserPrincipal = FintJwtEndUserPrincipal.from(jwt);

        if (applicationProperties.getDemo() && StringUtils.isNotEmpty(applicationProperties.getDemoUserEmployeeId())) {
            endUserPrincipal.setOrganizationNumber(applicationProperties.getDemoUserOrgId());
        }

        return principalService.getPrincipalByOrganisationId(endUserPrincipal.getOrganizationNumber(), endUserPrincipal.getEmployeeId());
    }
}
