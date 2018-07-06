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

    public <T> ResponseEntity setResource(Class<T> type, String url, T content, String orgId) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(content, getHeaders(orgId)),
                    type
            );
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to set %s url: %s", type.getSimpleName(), url), e);
        }
    }

    private HttpHeaders getHeaders(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        headers.set("x-client", "fint-betaling");
        return headers;
    }
}
