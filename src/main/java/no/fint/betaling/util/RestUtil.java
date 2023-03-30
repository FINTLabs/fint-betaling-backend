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
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class RestUtil {

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
                    .toFuture().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T get(Class<T> clazz, String uri) {
        return getFromFullUri(clazz, uri);
    }

    public <T> T getFromFullUri(Class<T> clazz, String endpoint) {
        // TODO: 06/05/2022 Dont string replace every time

        try {
            return webClient.get()
                    .uri(endpoint.replace(baseUrl, ""))
                    .retrieve()
                    .bodyToMono(clazz)
                    .toFuture()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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
                    .toFuture().get();
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> URI post(String uri, T content, Class<T> clazz, String orgId) {
        // todo: what is x-client id ??
        try {
            log.info("POST {} {}", uri, content);

            return webClient
                    .post()
                    .uri(uri)
                    .header("x-org-id", orgId)
                    .header ("x-client", "pwf.no")
                    .body(Mono.just(content), clazz)
                    .retrieve()
                    .toBodilessEntity()
                    .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                    .flatMap(entity -> Mono.justOrEmpty(entity.getHeaders()))
                    .toFuture()
                    .get()
                    .getLocation();
        } catch (HttpStatusCodeException e) {
            throw new InvalidResponseException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }
}
