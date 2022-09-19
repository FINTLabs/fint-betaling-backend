package no.fint.betaling;

import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fint.client")
public class OAuthConfiguration {

    private String baseUrl;
    private String username;
    private String password;
    private String registrationId;

    @Bean
    public Authentication dummyAuthentication() {
        return new UsernamePasswordAuthenticationToken("fint", "client", Collections.emptyList());
    }

    // rewrite this to use webflux NOT webmvc - questions about how to use the clientRegisterationRepsitory
    // spring-boot combining the both oAuth client and resource server
    // client -> mvc  resourceServer -> webflux
    @Bean
    @ConditionalOnProperty(name = "fint.betaling-backend.client-authorization.enable", havingValue = "true")
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .password()
                .refreshToken()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper());

        return authorizedClientManager;
    }


    private Function<OAuth2AuthorizeRequest, Map<String, Object>> contextAttributesMapper() {
        return authorizeRequest -> {
            Map<String, Object> contextAttributes = new HashMap<>();
            contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);
            contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);
            return contextAttributes;
        };
    }

    @Bean
    public ClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create(
                        ConnectionProvider
                                .builder("laidback")
                                .maxLifeTime(Duration.ofMinutes(30))
                                .maxIdleTime(Duration.ofMinutes(5))
                                .build())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300000)
                .responseTimeout(Duration.ofMinutes(5))
        );
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder, Optional<OAuth2AuthorizedClientManager> authorizedClientManager, ClientHttpConnector clientHttpConnector) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        authorizedClientManager.ifPresent(presentAuthorizedClientManager -> {
            ServletOAuth2AuthorizedClientExchangeFilterFunction authorizedClientExchangeFilterFunction =
                    new ServletOAuth2AuthorizedClientExchangeFilterFunction(presentAuthorizedClientManager);
            authorizedClientExchangeFilterFunction.setDefaultClientRegistrationId(registrationId);
            builder.filter(authorizedClientExchangeFilterFunction);
        });

        return builder
                .clientConnector(clientHttpConnector)
                .exchangeStrategies(exchangeStrategies)
                .baseUrl(baseUrl)
                .build();
    }

}

