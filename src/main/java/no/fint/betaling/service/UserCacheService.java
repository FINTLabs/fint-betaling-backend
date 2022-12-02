package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Service
public class UserCacheService {

    private final ConcurrentMap<String, User> users = new ConcurrentSkipListMap<>();

    private final UserRepository userRepository;

    public UserCacheService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String employeeId) {
        if (users.containsKey(employeeId)) {
            return users.get(employeeId);
        }

        User userFromSkoleressure = userRepository.mapUserFromResources(employeeId);
        users.put(employeeId, userFromSkoleressure);

        return userFromSkoleressure;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateUsers() {
        log.info("{} users needs to be updated ...", users.size());

        users.forEach((feideUpn, user) -> {
            User userFromSkoleressursByFeidenavn = userRepository.mapUserFromResources(feideUpn);
            users.put(feideUpn, userFromSkoleressursByFeidenavn);
        });
    }
}
