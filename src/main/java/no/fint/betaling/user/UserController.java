package no.fint.betaling.user;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.ApplicationProperties;
import no.fint.betaling.common.exception.EmployeeIdException;
import no.fint.betaling.common.exception.PersonalressursException;
import no.fint.betaling.model.User;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@RequestMapping(value = "/me")
@Slf4j
public class UserController {

    @Value("${fint.idle-time:900000}")
    private long idleTime;

    @Value("${fint.betaling.authorized-role-admin:https://role-catalog.vigoiks.no/vigo/elevfakturering/admin}")
    private String authorizedRoleAdmin;

    private final UserCacheService userCacheService;

    private final ApplicationProperties applicationProperties;

    private final UserRepository userRepository;

    public UserController(ApplicationProperties applicationProperties, UserCacheService userCacheService, UserRepository userRepository) {
        this.applicationProperties = applicationProperties;
        this.userCacheService = userCacheService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Mono<ResponseEntity<User>> getMe(@AuthenticationPrincipal Jwt jwt) {

        String employeeId;
        boolean isAdminUser = false;

        if (applicationProperties.getDemo() != null && applicationProperties.getDemo()) {
            employeeId = applicationProperties.getDemoUserEmployeeId();
        } else {
            FintJwtEndUserPrincipal userPrincipal = FintJwtEndUserPrincipal.from(jwt);
            employeeId = userPrincipal.getEmployeeId();
            isAdminUser = userPrincipal.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(authorizedRoleAdmin));
            log.debug("User " + employeeId + " is admin: " + isAdminUser);
        }

        if (StringUtils.isEmpty(employeeId)) {
            log.error("Brukerautorisering mangler employeeId!");
            return Mono.error(new EmployeeIdException(HttpStatus.BAD_REQUEST, "Brukerautorisering mangler nødvendig informasjon (employeeId)!"));
        }

        return userCacheService.getUser(employeeId, isAdminUser)
                .doOnNext(user -> {
                    user.setIdleTime(idleTime);
                    log.debug("User: {}", user);
                })
                .map(ResponseEntity::ok)
                .onErrorResume(PersonalressursException.class, ex -> {
                    log.error(ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(null));
                })
                .onErrorResume(ex -> {
                    log.error("An exception occured on handling getUser", ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() throws URISyntaxException {
        return ResponseEntity.ok("Greetings from FINTLabs :)");
    }

    @GetMapping("/{empolyenumber}")
    public ResponseEntity<Mono<String>> getEmployeeName(@PathVariable String empolyenumber) {
        return ResponseEntity.ok(userRepository.getNameFromEmplId(empolyenumber));
    }

    @ExceptionHandler({EmployeeIdException.class})
    public ResponseEntity handleEmployeeIdException(EmployeeIdException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
    }
}
