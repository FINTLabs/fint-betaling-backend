package no.fint.betaling.common.util;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RestUtil {

    @Setter
    @Value("${fint.client.base-url}")
    private String baseUrl;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    private final WebClient webClient;

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    public RestUtil(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> getUpdates(Class<T> clazz, String uri) {
        final String url = checkUrl(uri);
        return webClient.get()
                .uri(url.concat("/last-updated"))
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .flatMap(lastUpdated -> webClient.get()
                        .uri(url, uriBuilder -> uriBuilder.queryParam("sinceTimeStamp", sinceTimestamp.getOrDefault(url, 0L)).build())
                        .retrieve()
                        .bodyToMono(clazz)
                        .doOnNext(it -> sinceTimestamp.put(url, lastUpdated.getLastUpdated()))
                );
    }

    public <T> Mono<T> getWithRetry(Class<T> clazz, String uri) {
        uri = checkUrl(uri);
        return webClient.get()
                .uri(uri.replace(baseUrl, ""))
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .retrieve()
                .bodyToMono(clazz)
                .retryWhen(
                        Retry.backoff(5, Duration.ofSeconds(10))
                                .maxBackoff(Duration.ofSeconds(60))
                );
    }


    public <T> Mono<T> get(Class<T> clazz, String uri) {
        uri = checkUrl(uri);
        return webClient.get()
                .uri(uri.replace(baseUrl, ""))
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .retrieve()
                .bodyToMono(clazz);
    }

    public <T> Mono<T> getLive(Class<T> clazz, String uri) {
        URI url = URI.create(baseUrl).resolve(checkUrl(uri));
        return getLiveResponse(clazz, url, false).timeout(Duration.ofMinutes(2));
    }

    private <T> Mono<T> getLiveResponse(Class<T> clazz, URI url, boolean polling) {
        return webClient.get()
                .uri(url)
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .headers(headers -> {
                    if (!polling) headers.set("Prefer", "respond-async");
                })
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.ACCEPTED)) {
                        URI location = response.headers().asHttpHeaders().getLocation();
                        if (location == null && !polling) {
                            return response.releaseBody().then(Mono.error(
                                    new IllegalStateException("Live read returned 202 without a Location header")));
                        }
                        URI statusUrl = location == null ? url : url.resolve(location);
                        return response.releaseBody()
                                .then(Mono.delay(Duration.ofSeconds(2)))
                                .then(Mono.defer(() -> getLiveResponse(clazz, statusUrl, true)));
                    }
                    if (!response.statusCode().is2xxSuccessful()) {
                        return response.createException().flatMap(Mono::error);
                    }
                    return response.bodyToMono(clazz)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Live read returned an empty body")));
                });
    }

    public Mono<HttpHeaders> head(String uri) {
        uri = checkUrl(uri);
        log.info("GET {}", uri);

        return webClient
                .get()
                .uri(uri)
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .retrieve()
                .toBodilessEntity()
                .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                .flatMap(entity -> Mono.justOrEmpty(entity.getHeaders()));
    }

    public Mono<ResponseEntity<Void>> bodyless(String uri) {
        return bodyless(uri, HttpHeaders.EMPTY);
    }

    public Mono<ResponseEntity<Void>> bodyless(String uri, HttpHeaders headers) {
        uri = checkUrl(uri);
        log.info("GET {}", uri);

        return webClient
                .get()
                .uri(uri)
                .headers(requestHeaders -> requestHeaders.addAll(headers))
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .retrieve()
                .toBodilessEntity();
    }

    public <T> Mono<HttpHeaders> post(String uri, T content, Class<T> clazz) {
        uri = checkUrl(uri);
        // todo: what is x-client id ??
        log.info("POST {} {}", uri, content);

        return webClient
                .post()
                .uri(uri)
                .header("x-org-id", orgId)
                .header("x-client", "betaling")
                .body(Mono.just(content), clazz)
                .retrieve()
                .toBodilessEntity()
                .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                .flatMap(entity -> {
                    log.debug("POST returned {}", entity.getStatusCode());
                    return Mono.justOrEmpty(entity.getHeaders());
                });
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }

    // To not waste time on this, we just replace old urls
    private String checkUrl(String url) {
        return url.replace("beta1.felleskomponent.no", "api.felleskomponent.no");
    }
}
