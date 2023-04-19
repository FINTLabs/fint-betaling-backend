package no.fint.betaling.util;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RestUtil {

    @Setter
    @Value("${fint.client.base-url}")
    private String baseUrl;

    private final WebClient webClient;

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    public RestUtil(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T getUpdates(Class<T> clazz, String uri) {
        try {
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
        } catch (WebClientResponseException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    public <T> T get(Class<T> clazz, String uri) {
        try {
            return webClient.get()
                    .uri(uri.replace(baseUrl, ""))
                    .retrieve()
                    .bodyToMono(clazz)
                    .block();
        } catch (WebClientResponseException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    public <T> Mono<T> getMono(Class<T> clazz, String uri) {
        return webClient.get()
                .uri(uri.replace(baseUrl, ""))
                .retrieve()
                .bodyToMono(clazz);
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
        } catch (WebClientResponseException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

    public <T> Mono<HttpHeaders> post(String uri, T content, Class<T> clazz, String orgId) {
        // todo: what is x-client id ??
            log.info("POST {} {}", uri, content);

            return webClient
                    .post()
                    .uri(uri)
                    .header("x-org-id", orgId)
                    .header("x-client", "pwf.no")
                    .body(Mono.just(content), clazz)
                    .retrieve()
                    .toBodilessEntity()
                    .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                    .flatMap(entity -> Mono.justOrEmpty(entity.getHeaders()));
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }
}
