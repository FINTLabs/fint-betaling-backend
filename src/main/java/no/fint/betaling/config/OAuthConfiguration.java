package no.fint.betaling.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fint.client")
public class OAuthConfiguration {

    private String baseUrl;
    private String username;
    private String password;
    private String registrationId;
    private String enabled;

    @Bean
    @ConditionalOnProperty(value = "fint.client.enabled", havingValue = "true", matchIfMissing = true)
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .password()
                .refreshToken()
                .build();

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );

        if (StringUtils.isEmpty(username)) throw new IllegalArgumentException("Username cannot be empty!");
        if (StringUtils.isEmpty(password)) throw new IllegalArgumentException("Password cannot be empty!");

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        authorizedClientManager.setContextAttributesMapper(oAuth2AuthorizeRequest -> Mono.just(Map.of(
                OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username,
                OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password
        )));

        return authorizedClientManager;
    }

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistration.getRegistrationId())
                .principal(principal)
                .build();

        OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);

        if (isNull(client)) {
            throw new IllegalStateException("client credentials flow on " + clientRegistration.getRegistrationId() + " failed, client is null");
        }

        log.debug("Bearer " + client.getAccessToken().getTokenValue() +
                " - issued at : " + client.getAccessToken().getIssuedAt() +
                " - expired at : " + client.getAccessToken().getExpiresAt()
        );

        request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
        return execution.execute(request, body);
    }

    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(600000);
        simpleClientHttpRequestFactory.setReadTimeout(600000);
        return simpleClientHttpRequestFactory;
    }

    @Bean
    @ConditionalOnProperty(value = "fint.client.enabled", havingValue = "true", matchIfMissing = true)
    public RestClient restClient(
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager,
            ClientHttpRequestFactory clientHttpRequestFactory,
            ClientRegistrationRepository clientRegistrationRepository) {


        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("fint");
        //ClientHttpRequestInterceptor oauthInterceptor = new OAuthClientCredentialsRestTemplateInterceptor(authorizedClientManager, clientRegistration);
        //ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("custom");



        RestClient restClient = RestClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .requestFactory(clientHttpRequestFactory)
                //.requestInterceptor(oauthInterceptor)
                .baseUrl(baseUrl)
                .build();


//        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
//                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
//                .build();

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId(registrationId);

        return builder
                .clientConnector(clientHttpConnector)
                .exchangeStrategies(exchangeStrategies)
                .filter(oauth2Client)
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "fint.client.enabled", havingValue = "false")
    public RestClient webClientWithoutOauth(ClientHttpRequestFactory clientHttpRequestFactory) {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory)
                .baseUrl(baseUrl)
                .build();
    }
}

