package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.GroupRepository;
import no.fint.betaling.repository.OrganisationRepository;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
@Slf4j
public class MeController {

    @Value("${fint.betaling.endpoints.school-resource}")
    private String schoolResourceEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Cacheable("me")
    @GetMapping
    public User getMe(@RequestHeader(name = "x-ePPN", required = false) String ePPN) {
        User user = new User();

        SkoleressursResource skoleressurs = restUtil.get(SkoleressursResource.class,
                UriComponentsBuilder.fromUriString(schoolResourceEndpoint).pathSegment("feidenavn", ePPN).build().toUriString());

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
                .map(PersonResource::getNavn)
                .map(n -> StringUtils.isBlank(n.getMellomnavn())
                        ? String.format("%s %s", n.getFornavn(), n.getEtternavn())
                        : String.format("%s %s %s", n.getFornavn(), n.getMellomnavn(), n.getEtternavn()))
                .findFirst()
                .ifPresent(user::setName);

        List<SkoleResource> schools = skoleressurs
                .getSkole()
                .stream()
                .map(groupRepository.getSchools()::get)
                .peek(it -> log.debug("Skole: {}", it))
                .collect(Collectors.toList());

        user.setOrganisationUnits(
                schools
                        .stream()
                        .map(skole -> {
                            Organisation org = new Organisation();
                            org.setName(skole.getNavn());
                            org.setOrganisationNumber(skole.getOrganisasjonsnummer().getIdentifikatorverdi());
                            return org;
                        })
                        .collect(Collectors.toList()));

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

        log.info("User: {}", user);
        return user;
    }
}
