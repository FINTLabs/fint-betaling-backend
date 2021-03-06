package no.fint.betaling.util;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        // create the okhttp client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        ConnectionPool okHttpConnectionPool = new ConnectionPool(50, 15, TimeUnit.SECONDS);
        builder.connectionPool(okHttpConnectionPool);
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(false);

        // embed the created okhttp client to a spring rest template
        restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(builder.build()));
    }

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

    public HttpHeaders head(String uri) {
        try {
            log.info("GET {}", uri);
            return restTemplate.headForHeaders(uri);
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }


    public <T> T get(Class<T> clazz, String uri) {
        try {
            log.info("GET {}", uri);
            return restTemplate.getForObject(uri, clazz);
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    public <T> URI post(URI uri, T content) {
        try {
            log.info("POST {} {}", uri, content);
            return restTemplate.postForLocation(uri, content);
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }
}
