package no.fint.betaling.util;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RestUtil {

    @Setter
    @Value("${fint.client.base-url}")
    private String baseUrl;

    private final RestClient restClient;

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    public RestUtil(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T getUpdates(Class<T> clazz, String uri) {
        final String url = checkUrl(uri);
        return webClient.get()
                .uri(url.concat("/last-updated"))
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .flatMap(lastUpdated -> webClient.get()
                        .uri(url, uriBuilder -> uriBuilder.queryParam("sinceTimeStamp", sinceTimestamp.getOrDefault(url, 0L)).build())
                        .retrieve()
                        .bodyToMono(clazz)
                        .doOnNext(it -> sinceTimestamp.put(url, lastUpdated.getLastUpdated()))
                );
    }

    public <T> T getWhitRetry(Class<T> clazz, String uri) {
        uri = checkUrl(uri);
        return webClient.get()
                .uri(uri.replace(baseUrl, ""))
                .retrieve()
                .bodyToMono(clazz)
                .retryWhen(
                        Retry.backoff(5, Duration.ofSeconds(10))
                                .maxBackoff(Duration.ofSeconds(60))
                );
    }


    public <T> T get(Class<T> clazz, String uri) {
        uri = checkUrl(uri);
        return webClient.get()
                .uri(uri.replace(baseUrl, ""))
                .retrieve()
                .bodyToMono(clazz);
    }

    public HttpHeaders head(String uri) {
        uri = checkUrl(uri);
        log.info("GET {}", uri);

        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .filter(entity -> entity.getStatusCode().is2xxSuccessful())
                .flatMap(entity -> Mono.justOrEmpty(entity.getHeaders()));
    }

    public <T> HttpHeaders post(String uri, T content, Class<T> clazz, String orgId) {
        uri = checkUrl(uri);
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

    // To not waste time on this, we just replace old urls
    private String checkUrl(String url) {
        return url.replace("beta1.felleskomponent.no", "api.felleskomponent.no");
    }
}
