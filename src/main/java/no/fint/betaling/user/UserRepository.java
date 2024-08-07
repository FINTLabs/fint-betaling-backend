package no.fint.betaling.user;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.PersonNotFoundException;
import no.fint.betaling.common.exception.PersonalressursException;
import no.fint.betaling.fintdata.SchoolRepository;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import no.fint.betaling.common.util.FintClient;
import no.fint.betaling.fintdata.OrganisationRepository;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepository {

    private final OrganisationRepository organisationRepository;

    private final FintClient fintClient;
    private final SchoolRepository schoolRepository;

    public UserRepository(OrganisationRepository organisationRepository, FintClient fintClient, SchoolRepository schoolRepository) {
        this.organisationRepository = organisationRepository;
        this.fintClient = fintClient;
        this.schoolRepository = schoolRepository;
    }

    public Mono<User> mapUserFromResources(String employeeId, boolean isAdminUser) {

        return fintClient.getPersonalressurs(employeeId)
                .switchIfEmpty(Mono.error(new PersonalressursException(HttpStatus.BAD_REQUEST, "Fant ingen personalressurs for gitt ansatt.")))
                .flatMap(personalressurs -> {
                    User user = new User();
                    user.setEmployeeNumber(personalressurs.getAnsattnummer().getIdentifikatorverdi());

                    return fintClient.getPerson(personalressurs)
                            .map(personResource -> {
                                user.setName(getName(personResource));
                                return user;
                            })
                            .switchIfEmpty(Mono.error(new PersonNotFoundException("Fant ingen personalressurs for gitt ansatt.")))
                            .flatMap(isAdminUser ? adminUser -> handleAdminUser(adminUser) : nonAdminUser -> handleNonAdminUser(nonAdminUser, personalressurs));
                });
    }

    private Mono<User> handleAdminUser(User user) {
        List<SkoleResource> schools = getAllSchools();
        user.setOrganisationUnits(mapToOrganisation(schools));

        final Optional<Organisation> owner = getTopOrganisation(schools);
        owner.ifPresent(user::setOrganisation);

        return Mono.just(user);
    }

    private Mono<User> handleNonAdminUser(User user, PersonalressursResource personalressurs) {
        return getSchoolsBySkoleressurs(personalressurs)
                .switchIfEmpty(Mono.just(new ArrayList<SkoleResource>()))
                .flatMap(schools -> {
                    user.setOrganisationUnits(mapToOrganisation(schools));

                    final Optional<Organisation> owner = getTopOrganisation(schools);
                    owner.ifPresent(user::setOrganisation);

                    return Mono.just(user);
                });
    }

    private Mono<List<SkoleResource>> getSchoolsBySkoleressurs(PersonalressursResource personalressurs) {
        return fintClient
                .getSkoleressurs(personalressurs)
                .map(skoleressurs -> fintClient.getSkoler(skoleressurs));
    }

    private List<SkoleResource> getAllSchools() {
        log.debug("Fetching all distinct schools");
        return schoolRepository.getDistinctSchools();
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
