package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.ApplicationProperties;
import no.fint.betaling.exception.EmployeeIdException;
import no.fint.betaling.model.User;
import no.fint.betaling.service.UserCacheService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "/api/me")
@Slf4j
public class UserController {

    @Value("${fint.idle-time:900000}")
    private long idleTime;

    private final UserCacheService userCacheService;

    private ApplicationProperties applicationProperties;

    public UserController(ApplicationProperties applicationProperties, UserCacheService userCacheService) {
        this.applicationProperties = applicationProperties;
        this.userCacheService = userCacheService;
    }

    @GetMapping
    public User getMe(@AuthenticationPrincipal Jwt jwt) {

        String employeeId;

        if (applicationProperties.getDemo()) {
            employeeId = applicationProperties.getDemoUserEmployeeId();
        } else {
            employeeId = FintJwtEndUserPrincipal.from(jwt).getEmployeeId();
        }

        if (StringUtils.isEmpty(employeeId))
            throw new EmployeeIdException(HttpStatus.BAD_REQUEST, "Brukerautorisering mangler nødvendig informasjon (employeeId)!");

        User user = userCacheService.getUser(employeeId);
        user.setIdleTime(idleTime);
        log.debug("User: {}", user);

        return user;
    }

    @GetMapping("ping")
    public ResponseEntity<String> ping() throws URISyntaxException {
        return ResponseEntity.ok("Greetings from FINTLabs :)");
    }

    @ExceptionHandler({EmployeeIdException.class})
    public ResponseEntity handleEmployeeIdException(EmployeeIdException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getMessage());
    }
}
