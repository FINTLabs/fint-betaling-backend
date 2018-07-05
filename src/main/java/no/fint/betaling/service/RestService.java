package no.fint.betaling.service;

import no.fint.betaling.model.InvalidResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {

    @Autowired
    private RestTemplate restTemplate;

    public <T> T getResource(Class<T> type, String url, String orgId) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders(orgId)),
                    type
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get %s url: %s", type.getSimpleName(), url), e);
        }
    }

    public <T> boolean setResource(Class<T> type, String url, T content, String orgId) {
        HttpEntity<T> httpEntity = new HttpEntity<>(content, getHeaders(orgId));
        ResponseEntity responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                type
        );
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    private HttpHeaders getHeaders(String orgId){
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        headers.set("x-client", "fint-betaling");
        return headers;
    }
}
