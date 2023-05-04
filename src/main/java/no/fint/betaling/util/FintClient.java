package no.fint.betaling.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.Endpoints;
import no.fint.betaling.exception.SkoleressursException;
import no.fint.betaling.repository.GroupRepository;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FintClient {

    private final Endpoints endpoints;

    private final RestUtil restUtil;

    public final GroupRepository groupRepository;

    public FintClient(RestUtil restUtil, GroupRepository groupRepository, Endpoints endpoints) {
        this.restUtil = restUtil;
        this.groupRepository = groupRepository;
        this.endpoints = endpoints;
    }

    public Mono<PersonalressursResource> getPersonalressurs(String ansattnummer) {

        return restUtil.get(
                PersonalressursResource.class,
                UriComponentsBuilder.fromUriString(endpoints.getEmployee()).pathSegment("ansattnummer", ansattnummer).build().toUriString()
        );
    }

    public Mono<PersonResource> getPerson(PersonalressursResource personalressurs) {
        return restUtil.get(
                PersonResource.class,
                personalressurs.getPerson().get(0).getHref()
        );
    }

    public Mono<SkoleressursResource> getSkoleressurs(PersonalressursResource personalressurs) {
        AtomicReference<SkoleressursResource> result = new AtomicReference<>();
        personalressurs.getSelfLinks().forEach(selflink -> {
            if (groupRepository.getSchoolresources().containsKey(selflink)) {
                result.set(groupRepository.getSchoolresources().get(selflink));
            }
        });

        if (result.get() != null) return Mono.just(result.get());
        throw new SkoleressursException(HttpStatus.BAD_REQUEST, "Personalressursen har ingen relasjon til en skoleressurs");
    }

    public List<SkoleResource> getSkoler(SkoleressursResource skoleressurs) {
        return skoleressurs
                .getSkole()
                .stream()
                .map(groupRepository.getSchools()::get)
                .peek(it -> log.debug("Skole: {}", it))
                .collect(Collectors.toList());
    }
}
