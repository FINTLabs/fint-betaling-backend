package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.ApplicationProperties;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.MeRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "/api/me")
@Slf4j
public class MeController {

    @Value("${fint.idle-time:900000}")
    private long idleTime;

    private final MeRepository meRepository;

    private ApplicationProperties applicationProperties;

    public MeController(MeRepository meRepository, ApplicationProperties applicationProperties) {
        this.meRepository = meRepository;
        this.applicationProperties = applicationProperties;
    }

    @GetMapping
    public User getMe(@AuthenticationPrincipal Jwt jwt) {

        String employeeId;

        if (applicationProperties.getDemo()) {
            employeeId = applicationProperties.getDemoUserEmployeeId();
        } else {
            employeeId = FintJwtEndUserPrincipal.from(jwt).getEmployeeId();
        }

        User user = meRepository.getUserByAzureAD(employeeId);
        user.setIdleTime(idleTime);
        log.debug("User: {}", user);

        return user;
    }

    @GetMapping("ping")
    public ResponseEntity<String> ping() throws URISyntaxException {
        return ResponseEntity.ok("Greetings from FINTLabs :)");
    }

    @GetMapping("test")
    public ResponseEntity<User> test(@AuthenticationPrincipal Jwt jwt) throws URISyntaxException {

        FintJwtEndUserPrincipal endUserPrincipal = FintJwtEndUserPrincipal.from(jwt);
        String employeeId = endUserPrincipal.getEmployeeId();

        if (applicationProperties.getDemo()) {
            employeeId = applicationProperties.getDemoUserEmployeeId();
        }

        // User user = meRepository.getUserByAzureId(feideUpn);
        User user = meRepository.getUserByAzureAD(employeeId);
        user.setIdleTime(idleTime);
        log.debug("User: {}", user);

        return (ResponseEntity<User>) ResponseEntity
                .ok()
                .cacheControl(CacheControl.noStore())
                .body(user);

    }
}
