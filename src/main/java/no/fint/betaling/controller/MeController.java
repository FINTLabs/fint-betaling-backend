package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.MeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
@Slf4j
public class MeController {

    @Value("${fint.idle-time:900000}")
    private long idleTime;

    private final MeRepository meRepository;

    public MeController(MeRepository meRepository) {
        this.meRepository = meRepository;
    }

    @GetMapping
    public User getMe(@RequestHeader(name = "x-feide-upn") String feideUpn) {
        User user = meRepository.getUserByFeideUpn(feideUpn);
        user.setIdleTime(idleTime);
        log.debug("User: {}", user);

        return user;
    }

    @GetMapping("ping")
    public ResponseEntity<String> ping() throws URISyntaxException {
        return ResponseEntity.ok("Greatings for FINTLabs :)");
    }
}
