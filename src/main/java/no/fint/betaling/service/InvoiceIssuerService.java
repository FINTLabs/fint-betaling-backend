package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.PrincipalNotFoundException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.Principal;
import no.fint.betaling.repository.InvoiceIssuerRepository;
import no.fint.betaling.repository.UserRepository;
import no.fint.betaling.util.CloneUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.BiPredicate;

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
        return getInvoiceIssuer(organizationNumber, organisation, (p, orgNum) -> StringUtils.startsWithIgnoreCase(organisation.getName(), p.getDescription()));
    }

    private Principal getPrincipalFromOrgnummer(String organizationNumber, Organisation organisation) {
        return getInvoiceIssuer(organizationNumber, organisation, (p, orgNum) -> StringUtils.equalsIgnoreCase(p.getOrganisation().getOrganisationNumber(), orgNum));
    }

    private Principal getInvoiceIssuer(String organizationNumber, Organisation organisation, BiPredicate<Principal, String> filterCondition) {
        return invoiceIssuerRepository.getInvoiceIssuers()
                .stream()
                .filter(p -> filterCondition.test(p, organizationNumber))
                .map(CloneUtil::cloneObject)
                .peek(p -> p.setOrganisation(organisation))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Fakturautsteder with {} not found!", organizationNumber);
                    return new PrincipalNotFoundException(organizationNumber);
                });
    }
}
