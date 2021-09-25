package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.MeRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/me")
@Slf4j
public class MeController {

    private final MeRepository meRepository;

    public MeController(MeRepository meRepository) {
        this.meRepository = meRepository;
    }

    @GetMapping
    public User getMe(@RequestHeader(name = "x-feide-upn") String feideUpn) {
        User user = meRepository.getUserByFeideUpn(feideUpn);
        log.debug("User: {}", user);

        return user;
    }
}
