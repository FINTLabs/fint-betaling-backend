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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RestUtil {
    private final RestTemplate restTemplate;

    public RestUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${fint.betaling.client-name}")
    private String clientName;

    private final Map<String, Long> lastUpdatedMap = Collections.synchronizedMap(new HashMap<>());

    public <T> T getUpdates(Class<T> type, String url, String orgId) {
        String key = orgId + "_" + url;
        long lastUpdated = Long.parseLong(get(Map.class, url + "/last-updated", orgId).get("lastUpdated").toString());
        long since = lastUpdatedMap.getOrDefault(key, -1L) + 1L;
        log.info("{}: Fetching {} since {}, last updated {} ...", orgId, url, since, lastUpdated);
        T result = get(type, UriComponentsBuilder.fromUriString(url).queryParam("sinceTimeStamp", since).build().toUriString(), orgId);
        lastUpdatedMap.put(key, lastUpdated);
        return result;
    }

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
