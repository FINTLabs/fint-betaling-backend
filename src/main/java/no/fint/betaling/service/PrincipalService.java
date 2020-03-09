package no.fint.betaling.service;

import no.fint.betaling.exception.PrincipalNotFoundException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.Principal;
import no.fint.betaling.repository.PrincipalRepository;
import no.fint.betaling.util.CloneUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PrincipalService {

    private final OrganisationService organisationService;
    private final PrincipalRepository principalRepository;

    public PrincipalService(OrganisationService organisationService, PrincipalRepository principalRepository) {
        this.organisationService = organisationService;
        this.principalRepository = principalRepository;
    }

    public Principal getPrincipalByOrganisationId(String schoolId) {
        Organisation organisation = organisationService.getOrganisationByOrganisationNumber(schoolId);
        return principalRepository.getPrincipals()
                .stream()
                .filter(p -> StringUtils.equalsIgnoreCase(p.getDescription(), organisation.getName()))
                .map(CloneUtil::cloneObject)
                .peek(p -> p.setOrganisation(organisation))
                .findFirst()
                .orElseThrow(() -> new PrincipalNotFoundException(schoolId));
    }
}
