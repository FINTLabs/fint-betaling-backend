package no.fint.betaling.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RestUtil {

    @Value("${fint.betaling.endpoints.url-template:https://%s.felleskomponent.no%s}")
    private String urlTemplate;

    @Value("${fint.betaling.endpoints.environment}")
    private String environment;

    private final WebClient webClient;

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    public RestUtil(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T getUpdates(Class<T> clazz, String uri) {
        return webClient.get()
                .uri(uri.concat("/last-updated"))
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .flatMap(lastUpdated -> webClient.get()
                        .uri(uri, uriBuilder -> uriBuilder.queryParam("sinceTimeStamp", sinceTimestamp.getOrDefault(uri, 0L)).build())
                        .retrieve()
                        .bodyToMono(clazz)
                        .doOnNext(it -> sinceTimestamp.put(uri, lastUpdated.getLastUpdated()))
                )
                .block();
    }

    public <T> T get(Class<T> clazz, String uri) {
        return getFromFullUri(clazz, getFullUri(uri));
    }

    public <T> T getFromFullUri(Class<T> clazz, String endpoint) {
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    private String getFullUri(String uri) {
        return String.format(urlTemplate, environment, uri);
    }

    public HttpHeaders head(String uri) {
        try {
            log.info("GET {}", uri);

            return webClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toBodilessEntity()
                    .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                    .flatMap(entity -> Mono.justOrEmpty(entity.getHeaders()))
                    .block();
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    public <T> URI post(String uri, T content, Class<T> clazz) {
        try {
            log.info("POST {} {}", getFullUri(uri), content);

            return webClient
                    .post()
                    .uri(getFullUri(uri))
                    .body(Mono.just(content), clazz)
                    .retrieve()
                    .toBodilessEntity()
                    .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                    .flatMap(entity -> Mono.justOrEmpty(entity.getHeaders()))
                    .block()
                    .getLocation();
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }
}
