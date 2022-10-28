package no.fint.betaling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${fint.betaling.authorized-org-id:vigo.no}")
    private String authorizedOrgId;

    @Value("${fint.betaling.authorized-role:https://role-catalog.vigoiks.no/vigo/elevfakturering/user}")
    private String authorizedRole;

    @Value("${fint.betaling.authorized-role:https://role-catalog.vigoiks.no/vigo/elevfakturering/admin}")
    private String authorizedRoleAdmin;

    @Bean
    @ConditionalOnProperty(value="fint.betaling.demo", havingValue="true")
    SecurityWebFilterChain springSecurityFilterChainDemo(ServerHttpSecurity http) {

        return http
                .csrf()
                .disable()
                .httpBasic()
                .disable()
                .build();
    }

    @Bean
    @ConditionalOnProperty(value="fint.betaling.demo", havingValue="false", matchIfMissing=true)
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/**")
                        .hasAuthority("ORGID_" + authorizedOrgId)
                        .pathMatchers("/**")
                        //.hasAuthority("ROLE_" + authorizedRole)
                        .hasAnyAuthority("ROLE_" + authorizedRoleAdmin, "ROLE_" + authorizedRole)
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(new CustomUserConverter()));
        return http.build();
    }
}