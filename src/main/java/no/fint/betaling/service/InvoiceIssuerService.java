package no.fint.betaling.service;

import no.fint.betaling.exception.PrincipalNotFoundException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.Principal;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.InvoiceIssuerRepository;
import no.fint.betaling.util.CloneUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InvoiceIssuerService {

    @Value("${fint.betaling.principal-matching-strategy:default}")
    private String principalMatchingStrategy;

    private final OrganisationService organisationService;

    private final InvoiceIssuerRepository invoiceIssuerRepository;

    private final UserCacheService userCacheService;

    public InvoiceIssuerService(OrganisationService organisationService, InvoiceIssuerRepository invoiceIssuerRepository, UserCacheService userCacheService) {
        this.organisationService = organisationService;
        this.invoiceIssuerRepository = invoiceIssuerRepository;
        this.userCacheService = userCacheService;
    }

    public Principal getPrincipalByOrganisationId(String organizationNumber, String employeeId) {
        Organisation organisation = organisationService.getOrganisationByOrganisationNumber(organizationNumber);

        if (principalMatchingStrategy.equalsIgnoreCase("agder")) {
            User user = userCacheService.getUser(employeeId);

            return invoiceIssuerRepository.getInvoiceIssuers()
                    .stream()
                    .filter(p -> p.getOrganisation().getOrganisationNumber().equals(organizationNumber))
                    .filter(p -> p.getCode().endsWith("-" + user.getEmployeeNumber()))
                    .map(CloneUtil::cloneObject)
                    .peek(p -> p.setOrganisation(organisation))
                    .findFirst()
                    .orElseThrow(() -> new PrincipalNotFoundException(organizationNumber));
        }

        if (principalMatchingStrategy.equalsIgnoreCase("byOrgnummer")) {
            return invoiceIssuerRepository.getInvoiceIssuers()
                    .stream()
                    .filter(p -> StringUtils.equalsIgnoreCase(p.getOrganisation().getOrganisationNumber(), organizationNumber))
                    .map(CloneUtil::cloneObject)
                    .peek(p -> p.setOrganisation(organisation))
                    .findFirst()
                    .orElseThrow(() -> new PrincipalNotFoundException(organizationNumber));
        }

        return invoiceIssuerRepository.getInvoiceIssuers()
                .stream()
                .filter(p -> StringUtils.equalsIgnoreCase(p.getDescription(), organisation.getName()))
                .map(CloneUtil::cloneObject)
                .peek(p -> p.setOrganisation(organisation))
                .findFirst()
                .orElseThrow(() -> new PrincipalNotFoundException(organizationNumber));
    }
}
