package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.MeRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
@Slf4j
public class MeController {

    @Value("${fint.idle-time:900000}")
    private long idleTime;

    @Value("${fint.betaling.demo}")
    private Boolean isDemo;

    @Value("${fint.betaling.demo-user-employeeid}")
    private String demoUserEmployeeId;

    private final MeRepository meRepository;

    public MeController(MeRepository meRepository) {
        this.meRepository = meRepository;
    }

    @GetMapping
    public User getMe(@AuthenticationPrincipal Jwt jwt) {

        FintJwtEndUserPrincipal endUserPrincipal = FintJwtEndUserPrincipal.from(jwt);
        String employeeId = endUserPrincipal.getEmployeeId();

        // todo set up demo user
        if (isDemo && StringUtils.isNotEmpty(demoUserEmployeeId)) {
            employeeId = demoUserEmployeeId;
        }

        // User user = meRepository.getUserByAzureId(feideUpn);
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

        // todo set up demo user
        if (isDemo && StringUtils.isNotEmpty(demoUserEmployeeId)) {
            employeeId = demoUserEmployeeId;
        }

        // User user = meRepository.getUserByAzureId(feideUpn);
        User user = meRepository.getUserByAzureAD(employeeId);
        user.setIdleTime(idleTime);
        log.debug("User: {}", user);

        // return user;

        // return ResponseEntity.ok("hello world!");
        return (ResponseEntity<User>) ResponseEntity
                .ok()
                .cacheControl(CacheControl.noStore())
                .body(user);

    }
}
