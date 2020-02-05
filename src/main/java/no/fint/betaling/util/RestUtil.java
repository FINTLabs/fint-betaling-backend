package no.fint.betaling.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;

    private final Map<URI, Long> lastUpdatedMap = Collections.synchronizedMap(new HashMap<>());

    public <T> T getUpdates(Class<T> type, URI uri) {
        long lastUpdated = Long.parseLong(
                get(Map.class, UriComponentsBuilder
                                .fromUri(uri)
                                .pathSegment("last-updated")
                                .build()
                                .toUri())
                        .get("lastUpdated")
                        .toString()
        );
        long since = lastUpdatedMap.getOrDefault(uri, -1L) + 1L;
        log.info("Fetching {} since {}, last updated {} ...", uri, since, lastUpdated);
        T result = get(type, UriComponentsBuilder.fromUri(uri).queryParam("sinceTimeStamp", since).build().toUri());
        lastUpdatedMap.put(uri, lastUpdated);
        return result;
    }

    public <T> T get(Class<T> clazz, URI uri) {
        try {
            log.info("GET {}", uri);
            return restTemplate.getForObject(uri, clazz);
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    public <T> ResponseEntity<T> post(Class<T> clazz, URI uri, T content) {
        try {
            log.info("POST {} {}", uri, content);
            return restTemplate.postForEntity(uri, content, clazz);
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }
}
