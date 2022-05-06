package no.fint.betaling;

import no.vigoiks.resourceserver.security.FintJwtUserConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${fint.betaling.authorized-org-id:vigo.no}")
    private String authorizedOrgId;

    @Value("${fint.betaling.authorized-role:https://role-catalog.vigoiks.no/vigo/elevfakturering/user}")
    private String authorizedRole;

    // TODO: 06/05/2022 reenable this (Trond) 
  //  @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/**")
                        .hasAuthority("ORGID_" + authorizedOrgId)
                        .pathMatchers("/**")
                        .hasAuthority("ROLE_" + authorizedRole)
                        //.hasAnyAuthority("ORGID_" + authorizedOrgId, "ROLE_" + authorizedRole)

                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(new FintJwtUserConverter()));
        return http.build();
    }
}