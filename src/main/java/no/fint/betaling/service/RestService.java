package no.fint.betaling.service;

import no.fint.betaling.model.InvalidResponseException;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ELEV_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/elev/elev";
    private static final String BASISGRUPPE_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/elev/basisgruppe";
    private static final String UNDERVISNINGSGRUPPE_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/undervisningsgruppe";
    private static final String KONTAKTLARERGRUPPE_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/elev/kontaktlarergruppe";

    public BasisgruppeResources getBasisgruppeResources() {
        try {
            return restTemplate.exchange(
                    BASISGRUPPE_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    BasisgruppeResources.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get BasisgruppeResources url: %s", BASISGRUPPE_ENDPOINT), e);
        }
    }

    public UndervisningsgruppeResources getUndervisningsgruppeResources() {
        try {
            return restTemplate.exchange(
                    UNDERVISNINGSGRUPPE_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    UndervisningsgruppeResources.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get UndervisningsgruppeResources url: %s", UNDERVISNINGSGRUPPE_ENDPOINT), e);
        }
    }

    public KontaktlarergruppeResources getKontaktlarergruppeResources() {
        try {
            return restTemplate.exchange(
                    KONTAKTLARERGRUPPE_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    KontaktlarergruppeResources.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get KontaktlarergruppeResources url: %s", KONTAKTLARERGRUPPE_ENDPOINT), e);
        }
    }

    public MedlemskapResource getMedlemskapResource(String url) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    MedlemskapResource.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get MedlemskapResource url: %s", url), e);
        }
    }

    public ElevforholdResource getElevforholdResource(String url) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ElevforholdResource.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get ElevforholdResource url: %s", url), e);
        }
    }

    public ElevResources getElevResources() {
        try {
            return restTemplate.exchange(
                    ELEV_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    ElevResources.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get ElevResources url: %s", ELEV_ENDPOINT), e);
        }
    }

    public ElevResource getElevResource(String url) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ElevResource.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get ElevResource url: %s", url), e);
        }
    }

    public PersonResource getPersonResource(String url) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    PersonResource.class
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get PersonResource url: %s", url), e);
        }
    }
}
