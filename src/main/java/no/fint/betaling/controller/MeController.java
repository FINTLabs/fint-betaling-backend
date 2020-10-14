package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import no.fint.betaling.exception.PersonNotFoundException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.GroupRepository;
import no.fint.betaling.repository.OrganisationRepository;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.ArbeidsforholdResource;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
@Slf4j
public class MeController {

    @Value("${fint.betaling.endpoints.school-resource}")
    private String schoolResourceEndpoint;

    @Value("${fint.betaling.endpoints.employee}")
    private String employeeEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Cacheable("me")
    @GetMapping
    public User getMe(
            @RequestHeader(name = "x-ePPN", required = false) String ePPN,
            @RequestHeader(name = "x-nin", required = false) String nin
    ) {
        if (StringUtils.isNotBlank(ePPN)) {
            User user = getUserFromSkoleressursByFeidenavn(ePPN);

            log.debug("User: {}", user);
            return user;
        } else if (StringUtils.isNotBlank(nin)) {
            User user = getUserFromPersonalressursByNIN(nin);
            log.debug("User: {}", user);
            return user;
        }

        throw new IllegalArgumentException("User not found");
    }

    private User getUserFromPersonalressursByNIN(String nin) {
        User user = new User();

        PersonResource person = restUtil.get(PersonResource.class,
                UriComponentsBuilder.fromUriString(employeeEndpoint).pathSegment("fodselsnummer", nin).build().toUriString());

        log.debug("Person: {}", person);
        user.setName(getName(person));

        person
                .getPersonalressurs()
                .stream()
                .map(Link::getHref)
                .map(l -> restUtil.get(PersonalressursResource.class, l))
                .map(PersonalressursResource::getArbeidsforhold)
                .flatMap(List::stream)
                .map(Link::getHref)
                .map(l -> restUtil.get(ArbeidsforholdResource.class, l))
                .map(ArbeidsforholdResource::getArbeidssted)
                .flatMap(List::stream)
                .map(Link::getHref)
                .map(StringUtils::lowerCase)
                .map(organisationRepository::getTopOrganisationByHref)
                .distinct()
                .findAny()
                .ifPresent(user::setOrganisation);

        setOrganisationUnits(user, groupRepository.getSchools().values().stream().filter(s -> Objects.nonNull(s.getOrganisasjonsnummer())).distinct());

        return user;
    }

    private String getName(PersonResource person) {
        final Personnavn n = person.getNavn();
        return StringUtils.isBlank(n.getMellomnavn())
                ? String.format("%s %s", n.getFornavn(), n.getEtternavn())
                : String.format("%s %s %s", n.getFornavn(), n.getMellomnavn(), n.getEtternavn());
    }

    private User getUserFromSkoleressursByFeidenavn(String ePPN) throws PersonNotFoundException {
        User user = new User();
        SkoleressursResource skoleressurs;
        try {
            skoleressurs = restUtil.get(SkoleressursResource.class,
                    UriComponentsBuilder.fromUriString(schoolResourceEndpoint).pathSegment("feidenavn", ePPN).build().toUriString());
        } catch (InvalidResponseException ex) {
            if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                throw new PersonNotFoundException(ex.getMessage());
            }else{
                throw ex;
            }
        }

        log.debug("Skoleressurs: {}", skoleressurs);

        skoleressurs
                .getPersonalressurs()
                .stream()
                .map(Link::getHref)
                .map(it -> restUtil.get(PersonalressursResource.class, it))
                .peek(it -> log.debug("Personalressurs: {}", it))
                .flatMap(it -> it.getPerson().stream())
                .map(Link::getHref)
                .map(it -> restUtil.get(PersonResource.class, it))
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

    @ExceptionHandler({PersonNotFoundException.class})
    public ResponseEntity handlePersonNotFound() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "unable to find employee"));
    }
}
