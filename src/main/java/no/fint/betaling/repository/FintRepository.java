package no.fint.betaling.repository;

import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

public class FintRepository {

    @Value("${fint.betaling.endpoints.employee:/administrasjon/personal/personalressurs}")
    private String employeeEndpoint;

    private final RestUtil restUtil;

    public FintRepository(RestUtil restUtil) {
        this.restUtil = restUtil;
    }

    private PersonalressursResource getPersonalressurs(String ansattnummer) {

        return restUtil.get(
                PersonalressursResource.class,
                UriComponentsBuilder.fromUriString(employeeEndpoint).pathSegment("ansattnummer", ansattnummer).build().toUriString()
        );
    }
}
