package no.fint.betaling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.InvalidResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Service
public class RestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Value("${fint.betaling.client-name}")
    private String clientName;

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

    public <T> T getResource(Class<T> type, URI uri, String orgId) {
        try {
            return restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders(orgId)),
                    type
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get %s url: %s", type.getSimpleName(), uri), e);
        }
    }

    public <T> ResponseEntity setResource(Class<T> type, String url, T content, String orgId) {
        try {
            ResponseEntity responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(content, getHeaders(orgId)),
                    type
            );
            log.info(responseEntity.toString());
            return responseEntity;
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to set %s url: %s", type.getSimpleName(), url), e);
        }
    }


    private HttpHeaders getHeaders(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        headers.set("x-client", clientName);
        return headers;
    }
}
