package no.fint.betaling.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import no.fint.betaling.exception.SchoolNotFoundException;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class OrganisationService extends CacheLoader<String, Organisation> {

    private final String schoolEndpoint;
    private final String organisationEndpoint;
    private final RestUtil restUtil;
    private final LoadingCache<String, Organisation> organisationCache;

    public OrganisationService(
            @Value("${fint.betaling.endpoints.school}") String schoolEndpoint,
            @Value("${fint.betaling.endpoints.organisation}") String organisationEndpoint,
            RestUtil restUtil) {
        this.schoolEndpoint = schoolEndpoint;
        this.organisationEndpoint = organisationEndpoint;
        this.restUtil = restUtil;
        organisationCache = CacheBuilder.newBuilder().build(this);
    }

    private Organisation createOrganisation(Identifikator id, String... names) {
        Organisation org = new Organisation();
        Arrays.stream(names).filter(StringUtils::isNotBlank).findFirst().ifPresent(org::setName);
        org.setOrganisationNumber(id.getIdentifikatorverdi());
        return org;
    }

    @Override
    public Organisation load(String key) {
        try {
            SkoleResource skoleResource = restUtil.get(SkoleResource.class,
                    UriComponentsBuilder.fromUriString(schoolEndpoint).pathSegment("organisasjonsnummer", key).build().toUriString());
            return createOrganisation(skoleResource.getOrganisasjonsnummer(),
                    skoleResource.getJuridiskNavn(),
                    skoleResource.getNavn(),
                    skoleResource.getOrganisasjonsnavn());
        } catch (InvalidResponseException e) {
            log.info("School {} not found: {} {}", key, e.getStatus(), e.getMessage());
            OrganisasjonselementResource organisasjonselementResource = restUtil.get(OrganisasjonselementResource.class,
                    UriComponentsBuilder.fromUriString(organisationEndpoint).pathSegment("organisasjonsnummer", key).build().toUriString());
            return createOrganisation(organisasjonselementResource.getOrganisasjonsnummer(),
                    organisasjonselementResource.getNavn(),
                    organisasjonselementResource.getOrganisasjonsnavn(),
                    organisasjonselementResource.getKortnavn());
        }
    }

    public Organisation getOrganisationByOrganisationNumber(String id) {
        try {
            return organisationCache.get(id);
        } catch (ExecutionException e) {
            log.info("Cannot find {}", id, e);
            throw new SchoolNotFoundException(id + ": " + e.getMessage());
        }
    }
}
