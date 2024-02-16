package no.fint.betaling.config;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.CustomUserConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${fint.betaling.org-id}")
    private String orgId;

    @Value("${fint.betaling.authorized-role:https://role-catalog.vigoiks.no/vigo/elevfakturering/user}")
    private String authorizedRole;

    @Value("${fint.betaling.authorized-role-admin:https://role-catalog.vigoiks.no/vigo/elevfakturering/admin}")
    private String authorizedRoleAdmin;

    @Bean
    @ConditionalOnProperty(value = "fint.betaling.demo", havingValue = "true")
    SecurityWebFilterChain springSecurityFilterChainDemo(ServerHttpSecurity http) {

        log.warn("Starting WITHOUT security. SecurityWebFilterChain disabled.");

        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "fint.betaling.demo", havingValue = "false", matchIfMissing = true)
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/**")
                        .access(this::hasRequiredOrgIdAndRole)
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(new CustomUserConverter())));
        return http.build();
    }

    private Mono<AuthorizationDecision> hasRequiredOrgIdAndRole(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication.map(auth -> {

            // TEMPORARY CODE: To test authentication and authorization
            boolean isFintUser = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ORGID_vigo.no"));

            if (isFintUser) {
                log.debug("Authorize as fint user");
                return new AuthorizationDecision(true);
            }
            // END OF TEMPORARY CODE

            boolean hasOrgId = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ORGID_" + orgId));
            boolean hasRole = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + authorizedRole));
            boolean hasAdminRole = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + authorizedRoleAdmin));

            AuthorizationDecision authorizationDecision = new AuthorizationDecision(hasOrgId && (hasRole || hasAdminRole));
            log.debug("Authorize: {} is granted: {} ({} {} {})", auth.getName(), authorizationDecision.isGranted(), hasOrgId, hasRole, hasAdminRole);
            return authorizationDecision;
        });
    }
}