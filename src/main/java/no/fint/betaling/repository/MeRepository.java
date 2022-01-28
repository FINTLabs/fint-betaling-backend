package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import no.fint.betaling.util.FintEndpointsRepository;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
public class MeRepository {

    @Value("${fint.betaling.endpoints.school-resource:/utdanning/elev/skoleressurs}")
    private String schoolResourceEndpoint;

    @Value("${fint.betaling.endpoints.employee:/administrasjon/personal/person}")
    private String employeeEndpoint;

    private final FintEndpointsRepository fintEndpointsRepository;
    private final GroupRepository groupRepository;
    private final OrganisationRepository organisationRepository;

    private final ConcurrentMap<String, User> users = new ConcurrentSkipListMap<>();


    public MeRepository(FintEndpointsRepository fintEndpointsRepository, GroupRepository groupRepository, OrganisationRepository organisationRepository) {
        this.fintEndpointsRepository = fintEndpointsRepository;
        this.groupRepository = groupRepository;
        this.organisationRepository = organisationRepository;
    }

    public User getUserByFeideUpn(String feideUpn) {
        if (users.containsKey(feideUpn)) {
            return users.get(feideUpn);
        }
        User userFromSkoleressursByFeidenavn = getUserFromSkoleressursByFeidenavn(feideUpn);
        users.put(feideUpn, userFromSkoleressursByFeidenavn);

        return userFromSkoleressursByFeidenavn;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateUsers() {
        log.info("{} users needs to be updated ...", users.size());

        users.forEach((feideUpn, user) -> {
            User userFromSkoleressursByFeidenavn = getUserFromSkoleressursByFeidenavn(feideUpn);
            users.put(feideUpn, userFromSkoleressursByFeidenavn);
        });

    }

    private String getName(PersonResource person) {
        final Personnavn n = person.getNavn();
        return StringUtils.isBlank(n.getMellomnavn())
                ? String.format("%s %s", n.getFornavn(), n.getEtternavn())
                : String.format("%s %s %s", n.getFornavn(), n.getMellomnavn(), n.getEtternavn());
    }

    private User getUserFromSkoleressursByFeidenavn(String feideUpn) {
        User user = new User();

        SkoleressursResource skoleressurs = fintEndpointsRepository.get(SkoleressursResource.class,
                UriComponentsBuilder.fromUriString(schoolResourceEndpoint).pathSegment("feidenavn", feideUpn).build().toUriString());

        log.debug("Skoleressurs: {}", skoleressurs);

        skoleressurs
                .getPersonalressurs()
                .stream()
                .map(Link::getHref)
                .map(it -> fintEndpointsRepository.getFromFullUri(PersonalressursResource.class, it))
                .peek(it -> log.debug("Personalressurs: {}", it))
                .peek(it -> user.setEmployeeNumber(it.getAnsattnummer().getIdentifikatorverdi()))
                .flatMap(it -> it.getPerson().stream())
                .map(Link::getHref)
                .map(it -> fintEndpointsRepository.getFromFullUri(PersonResource.class, it))
                .peek(it -> log.debug("Person: {}", it))
                .map(this::getName)
                .findFirst()
                .ifPresent(user::setName);

        List<SkoleResource> schools = skoleressurs
                .getSkole()
                .stream()
                .map(groupRepository.getSchools()::get)
                .peek(it -> log.debug("Skole: {}", it))
                .collect(Collectors.toList());

        setOrganisationUnits(user, schools.stream());

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

    private void setOrganisationUnits(User user, Stream<SkoleResource> schools) {
        user.setOrganisationUnits(
                schools
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
