package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.PersonNotFoundException;
import no.fint.betaling.exception.PersonalressursException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import no.fint.betaling.util.FintClient;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepository {

    private final OrganisationRepository organisationRepository;

    private final GroupRepository groupRepository;

    private final FintClient fintClient;

    public UserRepository(OrganisationRepository organisationRepository, GroupRepository groupRepository, FintClient fintClient) {
        this.organisationRepository = organisationRepository;
        this.groupRepository = groupRepository;
        this.fintClient = fintClient;
    }

    public User mapUserFromResources(String employeeId, boolean isAdminUser) {

        PersonalressursResource personalressurs = fintClient.getPersonalressurs(employeeId);
        if (personalressurs == null) {
            throw new PersonalressursException(HttpStatus.BAD_REQUEST, "Fant ingen personalressurs for gitt ansatt.");
        }

        User user = new User();
        user.setEmployeeNumber(personalressurs.getAnsattnummer().getIdentifikatorverdi());

        PersonResource personResource = fintClient.getPerson(personalressurs);
        if (personResource == null) {
            throw new PersonNotFoundException("Fant ingen personalressurs for gitt ansatt.");
        }

        user.setName(getName(personResource));
        return isAdminUser ? handleAdminUser(user) : handleNonAdminUser(user, personalressurs);
    }

    private User handleAdminUser(User user) {
        List<SkoleResource> schools = getAllSchools();
        user.setOrganisationUnits(mapToOrganisation(schools));

        final Optional<Organisation> owner = getTopOrganisation(schools);
        owner.ifPresent(user::setOrganisation);

        return user;
    }

    private User handleNonAdminUser(User user, PersonalressursResource personalressurs) {
        List<SkoleResource> schools = getSchoolsBySkoleressurs(personalressurs);
        if (schools == null) {
            return user;
        }

        user.setOrganisationUnits(mapToOrganisation(schools));

        final Optional<Organisation> owner = getTopOrganisation(schools);
        owner.ifPresent(user::setOrganisation);

        return user;
    }

    private List<SkoleResource> getSchoolsBySkoleressurs(PersonalressursResource personalressurs) {
        SkoleressursResource skoleressurs = fintClient.getSkoleressurs(personalressurs);
        return fintClient.getSkoler(skoleressurs);
    }

    private List<SkoleResource> getAllSchools() {
        log.debug("Fetching all distinct schools");
        return groupRepository.getDistinctSchools();
    }

    private String getName(PersonResource person) {
        final Personnavn n = person.getNavn();
        return StringUtils.isBlank(n.getMellomnavn())
                ? "%s %s".formatted(n.getFornavn(), n.getEtternavn())
                : "%s %s %s".formatted(n.getFornavn(), n.getMellomnavn(), n.getEtternavn());
    }

    private Optional<Organisation> getTopOrganisation(List<SkoleResource> schools) {
        return schools
                .stream()
                .map(SkoleResource::getOrganisasjon)
                .flatMap(List::stream)
                .map(Link::getHref)
                .map(StringUtils::lowerCase)
                .map(organisationRepository::getTopOrganisationByHref)
                .distinct()
                .peek(it -> log.debug("Organisasjon: {}", it))
                .findAny();
    }

    private List<Organisation> mapToOrganisation(List<SkoleResource> schools) {
        return schools
                .stream()
                .map(skole -> {
                    Organisation org = new Organisation();
                    org.setName(skole.getNavn());
                    if (skole.getOrganisasjonsnummer() != null) {
                        org.setOrganisationNumber(skole.getOrganisasjonsnummer().getIdentifikatorverdi());
                    }
                    return org;
                })
                .sorted(Comparator.comparing(Organisation::getName))
                .collect(Collectors.toList());
    }
}
