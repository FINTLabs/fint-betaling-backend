package no.fint.betaling.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Component
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;

    private final ConcurrentMap<String, Long> lastUpdatedMap = new ConcurrentSkipListMap<>();

    public <T> T getUpdates(Class<T> type, String uri) {
        long lastUpdated = Long.parseLong(
                get(Map.class, UriComponentsBuilder
                                .fromUriString(uri)
                                .pathSegment("last-updated")
                                .build()
                                .toUriString())
                        .get("lastUpdated")
                        .toString()
        );
        long since = lastUpdatedMap.getOrDefault(uri, -1L) + 1L;
        log.info("Fetching {} since {}, last updated {} ...", uri, since, lastUpdated);
        T result = get(type, UriComponentsBuilder.fromUriString(uri).queryParam("sinceTimeStamp", since).build().toUriString());
        lastUpdatedMap.put(uri, lastUpdated);
        return result;
    }

    public <T> T get(Class<T> clazz, String uri) {
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
