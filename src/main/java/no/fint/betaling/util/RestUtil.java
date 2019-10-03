package no.fint.betaling.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${fint.betaling.client-name}")
    private String clientName;

    public <T> T get(Class<T> type, String url, String orgId) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders(orgId)),
                    type
            ).getBody();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get %s url: %s: %s", type.getSimpleName(), url, e.getMessage()), e);
        }
    }

    public <T> ResponseEntity<T> post(Class<T> type, String url, T content, String orgId) {
        try {
            log.info("POST {} {}", url, content);
            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(content, getHeaders(orgId)),
                    type
            );
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to set %s url: %s: %s", type.getSimpleName(), url, e.getMessage()), e);
        }
    }


    private HttpHeaders getHeaders(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        headers.set("x-client", clientName);
        return headers;
    }
}
