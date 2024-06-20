package no.fint.betaling.invoiceissuer;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.PrincipalNotFoundException;
import no.fint.betaling.common.util.CloneUtil;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.Principal;
import no.fint.betaling.organisation.OrganisationService;
import no.fint.betaling.user.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class InvoiceIssuerService {

    @Value("${fint.betaling.principal-matching-strategy:default}")
    private String principalMatchingStrategy;

    private final OrganisationService organisationService;

    private final InvoiceIssuerRepository invoiceIssuerRepository;

    public InvoiceIssuerService(OrganisationService organisationService, InvoiceIssuerRepository invoiceIssuerRepository, UserRepository userRepository) {
        this.organisationService = organisationService;
        this.invoiceIssuerRepository = invoiceIssuerRepository;
    }

    public Mono<Principal> getInvoiceIssuer(String organizationNumber) {
        return organisationService.getOrganisationByOrganisationNumber(organizationNumber)
                .switchIfEmpty(Mono.error(new PrincipalNotFoundException(organizationNumber)))
                .map(organisation -> getPrincipal(organizationNumber, organisation));
    }

    private Principal getPrincipal(String organizationNumber, Organisation organisation) {
        return principalMatchingStrategy.equalsIgnoreCase("byOrgnummer")
                ? getPrincipalFromOrgnummer(organizationNumber, organisation)
                : getPrincipalByName(organizationNumber, organisation);
    }

    private Principal getPrincipalByName(String organizationNumber, Organisation organisation) {
        return invoiceIssuerRepository.getInvoiceIssuers()
                .stream()
                .filter(p -> StringUtils.startsWithIgnoreCase(organisation.getName(), p.getDescription()))
                .map(CloneUtil::cloneObject)
                .peek(p1 -> p1.setOrganisation(organisation))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Fakturautsteder with matching name not found! Organisation: {}, Organization Number: {}", organisation.getName(), organizationNumber);
                    return new PrincipalNotFoundException(organizationNumber);
                });
    }

    private Principal getPrincipalFromOrgnummer(String organizationNumber, Organisation organisation) {
        return invoiceIssuerRepository.getInvoiceIssuers()
                .stream()
                .filter(p -> StringUtils.equalsIgnoreCase(p.getOrganisation().getOrganisationNumber(), organizationNumber))
                .map(CloneUtil::cloneObject)
                .peek(p1 -> p1.setOrganisation(organisation))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Fakturautsteder with matching organizationNumber not found! OrgNumber: {}", organizationNumber);
                    return new PrincipalNotFoundException(organizationNumber);
                });
    }
}
