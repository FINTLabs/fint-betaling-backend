package no.fint.betaling.service;

import no.fint.betaling.model.InvalidResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {

    @Autowired
    private RestTemplate restTemplate;

    static final String ELEV_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/elev/elev";
    static final String BASISGRUPPE_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/elev/basisgruppe";
    static final String UNDERVISNINGSGRUPPE_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/undervisningsgruppe";
    static final String KONTAKTLARERGRUPPE_ENDPOINT = "https://play-with-fint.felleskomponent.no/utdanning/elev/kontaktlarergruppe";

    public <T> T getResource(Class<T> type, String url, String orgId) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    type
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get %s url: %s", type.getSimpleName(), url), e);
        }
    }
}
