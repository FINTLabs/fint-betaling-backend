package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.PersonalressursException;
import no.fint.betaling.exception.SkoleressursException;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FintRepository {

    @Value("${fint.betaling.endpoints.employee:/administrasjon/personal/personalressurs}")
    private String employeeEndpoint;

    private final RestUtil restUtil;

    public final GroupRepository groupRepository;

    public FintRepository(RestUtil restUtil, GroupRepository groupRepository) {
        this.restUtil = restUtil;
        this.groupRepository = groupRepository;
    }

    public PersonalressursResource getPersonalressurs(String ansattnummer) {

        PersonalressursResource personalressurs = restUtil.get(
                PersonalressursResource.class,
                UriComponentsBuilder.fromUriString(employeeEndpoint).pathSegment("ansattnummer", ansattnummer).build().toUriString()
        );

        if (personalressurs == null) {
            log.error("Did not find any Personalressurs for empoloyeeId=" + ansattnummer);
            throw new PersonalressursException(HttpStatus.BAD_REQUEST, "Fant ingen personalressurs for gitt ansatt.");
        }

        return personalressurs;
    }

    public PersonResource getPerson(PersonalressursResource personalressurs) {
        return restUtil.getFromFullUri(
                PersonResource.class,
                personalressurs.getPerson().get(0).getHref()
        );
    }

    public SkoleressursResource getSkoleressurs(PersonalressursResource personalressurs){
        if (personalressurs.getSkoleressurs().size() == 0)
            throw new SkoleressursException(HttpStatus.BAD_REQUEST, "Personalressursen har ingen relasjon til en skoleressurs");

        SkoleressursResource skoleressurs = restUtil.get(SkoleressursResource.class, personalressurs.getSkoleressurs().get(0).getHref());
        log.debug("Skoleressurs: {}", skoleressurs);

        return skoleressurs;
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
