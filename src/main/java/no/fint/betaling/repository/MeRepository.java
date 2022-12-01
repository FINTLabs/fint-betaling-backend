package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.PersonalressursException;
import no.fint.betaling.exception.SkoleressursException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MeRepository {

    @Value("${fint.betaling.endpoints.school-resource:/utdanning/elev/skoleressurs}")
    private String schoolResourceEndpoint;

    private final RestUtil restUtil;

    private final GroupRepository groupRepository;

    private final OrganisationRepository organisationRepository;

    private final FintRepository fintRepository;

    private final ConcurrentMap<String, User> users = new ConcurrentSkipListMap<>();

    public MeRepository(RestUtil restUtil, GroupRepository groupRepository, OrganisationRepository organisationRepository, FintRepository fintRepository) {
        this.restUtil = restUtil;
        this.groupRepository = groupRepository;
        this.organisationRepository = organisationRepository;
        this.fintRepository = fintRepository;
    }

    public User getUserByAzureAD(String employeeId) {
        if (users.containsKey(employeeId)) {
            return users.get(employeeId);
        }

        User userFromSkoleressure = getUserFromSkoleressure(employeeId);
        users.put(employeeId, userFromSkoleressure);

        return userFromSkoleressure;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateUsers() {
        log.info("{} users needs to be updated ...", users.size());

        users.forEach((feideUpn, user) -> {
            User userFromSkoleressursByFeidenavn = getUserFromSkoleressure(feideUpn);
            users.put(feideUpn, userFromSkoleressursByFeidenavn);
        });

    }

    private String getName(PersonResource person) {
        final Personnavn n = person.getNavn();
        return StringUtils.isBlank(n.getMellomnavn())
                ? String.format("%s %s", n.getFornavn(), n.getEtternavn())
                : String.format("%s %s %s", n.getFornavn(), n.getMellomnavn(), n.getEtternavn());
    }

    private User getUserFromSkoleressure(String employeeId) {
        User user = new User();

        PersonalressursResource personalressurs =
                restUtil.get(
                        PersonalressursResource.class,
                        UriComponentsBuilder.fromUriString(employeeEndpoint).pathSegment("ansattnummer", employeeId).build().toUriString()
                );

        if (personalressurs == null) {
            log.error("Did not find any Personalressurs for empoloyeeId=" + employeeId);
            throw new PersonalressursException(HttpStatus.BAD_REQUEST, "Fant ingen personalressurs for gitt ansatt.");
        }

        user.setEmployeeNumber(personalressurs.getAnsattnummer().getIdentifikatorverdi());

        PersonResource person = restUtil.getFromFullUri(PersonResource.class, personalressurs.getPerson().get(0).getHref());
        user.setName(getName(person));

        if (personalressurs.getSkoleressurs().size() == 0)
            throw new SkoleressursException(HttpStatus.BAD_REQUEST, "Personalressursen har ingen relasjon til en skoleressurs");

        SkoleressursResource skoleressurs = restUtil.get(SkoleressursResource.class, personalressurs.getSkoleressurs().get(0).getHref());
        log.debug("Skoleressurs: {}", skoleressurs);

        List<SkoleResource> schools = skoleressurs
                .getSkole()
                .stream()
                .map(groupRepository.getSchools()::get)
                .peek(it -> log.debug("Skole: {}", it))
                .collect(Collectors.toList());

        setOrganisationUnits(user, schools);

        final Optional<Organisation> owner = schools
                .stream()
                .map(SkoleResource::getOrganisasjon)
                .flatMap(List::stream)
                .map(Link::getHref)
                .map(StringUtils::lowerCase)
                .map(organisationRepository::getTopOrganisationByHref)
                .distinct()
                .peek(it -> log.debug("Organisasjon: {}", it))
                .findAny();

        owner.ifPresent(user::setOrganisation);
        return user;
    }

    private void setOrganisationUnits(User user, List<SkoleResource> schools) {
        user.setOrganisationUnits(
                schools
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
                        .collect(Collectors.toList()));
    }
}
