package no.fint.betaling.service;

import no.fint.betaling.exception.PrincipalNotFoundException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.Principal;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.MeRepository;
import no.fint.betaling.repository.PrincipalRepository;
import no.fint.betaling.util.CloneUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PrincipalService {

    @Value("${fint.betaling.principal-matching-strategy:default}")
    private String principalMatchingStrategy;

    private final OrganisationService organisationService;
    private final PrincipalRepository principalRepository;
    private final MeRepository meRepository;

    public PrincipalService(OrganisationService organisationService, PrincipalRepository principalRepository, MeRepository meRepository) {
        this.organisationService = organisationService;
        this.principalRepository = principalRepository;
        this.meRepository = meRepository;
    }

    public Principal getPrincipalByOrganisationId(String schoolId, String feideUpn) {
        Organisation organisation = organisationService.getOrganisationByOrganisationNumber(schoolId);
        User user = meRepository.getUserByFeideUpn(feideUpn);

        if (principalMatchingStrategy.equalsIgnoreCase("agder")) {
            return principalRepository.getPrincipals()
                    .stream()
                    .filter(p -> p.getOrganisation().getOrganisationNumber().equals(schoolId))
                    .filter(p -> p.getCode().endsWith("-" + user.getEmployeeNumber()))
                    .map(CloneUtil::cloneObject)
                    .peek(p -> p.setOrganisation(organisation))
                    .findFirst()
                    .orElseThrow(() -> new PrincipalNotFoundException(schoolId));
        }

        return principalRepository.getPrincipals()
                .stream()
                .filter(p -> StringUtils.equalsIgnoreCase(p.getDescription(), organisation.getName()))
                .map(CloneUtil::cloneObject)
                .peek(p -> p.setOrganisation(organisation))
                .findFirst()
                .orElseThrow(() -> new PrincipalNotFoundException(schoolId));
    }
}
