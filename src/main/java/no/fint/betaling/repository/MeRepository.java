package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MeRepository {

    private final OrganisationRepository organisationRepository;

    private final FintRepository fintRepository;

    private final ConcurrentMap<String, User> users = new ConcurrentSkipListMap<>();

    public MeRepository(OrganisationRepository organisationRepository, FintRepository fintRepository) {
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

    private User getUserFromSkoleressure(String employeeId) {
        User user = new User();

        PersonalressursResource personalressurs = fintRepository.getPersonalressurs(employeeId);
        user.setEmployeeNumber(personalressurs.getAnsattnummer().getIdentifikatorverdi());
        user.setName(getName(fintRepository.getPerson(personalressurs)));

        SkoleressursResource skoleressurs = fintRepository.getSkoleressurs(personalressurs);
        List<SkoleResource> schools = fintRepository.getSkoler(skoleressurs);
        user.setOrganisationUnits(mapToOrganisation(schools));

        final Optional<Organisation> owner = getTopOrganisation(schools);
        owner.ifPresent(user::setOrganisation);

        return user;
    }

    private String getName(PersonResource person) {
        final Personnavn n = person.getNavn();
        return StringUtils.isBlank(n.getMellomnavn())
                ? String.format("%s %s", n.getFornavn(), n.getEtternavn())
                : String.format("%s %s %s", n.getFornavn(), n.getMellomnavn(), n.getEtternavn());
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
