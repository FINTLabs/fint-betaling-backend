package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

@Slf4j
@Service
public class OrganisationService {

    private final String schoolEndpoint;
    private final String organisationEndpoint;
    private final RestUtil restUtil;

    public OrganisationService(
            @Value("${fint.betaling.endpoints.school:/utdanning/utdanningsprogram/skole}") String schoolEndpoint,
            @Value("${fint.betaling.endpoints.organisation:/administrasjon/organisasjon/organisasjonselement}") String organisationEndpoint,
            RestUtil restUtil) {
        this.schoolEndpoint = schoolEndpoint;
        this.organisationEndpoint = organisationEndpoint;
        this.restUtil = restUtil;
    }

    private Organisation createOrganisation(Identifikator id, String... names) {
        Organisation org = new Organisation();
        Arrays.stream(names).filter(StringUtils::isNotBlank).findFirst().ifPresent(org::setName);
        org.setOrganisationNumber(id.getIdentifikatorverdi());
        return org;
    }

    @Cacheable("organisations")
    public Organisation getOrganisationByOrganisationNumber(String id) {
        String uri = UriComponentsBuilder.fromUriString(schoolEndpoint).pathSegment("organisasjonsnummer", id).build().toUriString();

        try {
            SkoleResource skoleResource = restUtil.get(SkoleResource.class, uri);
            return createOrganisation(skoleResource.getOrganisasjonsnummer(), skoleResource.getJuridiskNavn(), skoleResource.getNavn(), skoleResource.getOrganisasjonsnavn());
        } catch (WebClientResponseException e) {
            log.warn("School {} not found: {} {}", id, e.getStatusCode(), e.getMessage());
            return e.getStatusCode() == HttpStatus.NOT_FOUND ? getOrganisationFromOrganisasjonselement(id) : Mono.error(e);
        }
    }

    private Organisation getOrganisationFromOrganisasjonselement(String id) {
        String orgUri = UriComponentsBuilder.fromUriString(organisationEndpoint).pathSegment("organisasjonsnummer", id).build().toUriString();
        OrganisasjonselementResource organisasjonselementResource = restUtil.get(OrganisasjonselementResource.class, orgUri);

        return createOrganisation(organisasjonselementResource.getOrganisasjonsnummer(),
                organisasjonselementResource.getNavn(),
                organisasjonselementResource.getOrganisasjonsnavn(),
                organisasjonselementResource.getKortnavn());
    }
}
