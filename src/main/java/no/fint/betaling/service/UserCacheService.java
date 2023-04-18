package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.User;
import no.fint.betaling.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<User> getUser(String employeeId, boolean isAdminUser) {
        if (users.containsKey(employeeId)) {
            User user = users.get(employeeId);
            user.setAdmin(isAdminUser);
            return Mono.just(user);
        }

        return userRepository.mapUserFromResources(employeeId, isAdminUser)
                .map(user -> {
                    user.setAdmin(isAdminUser);
                    users.put(employeeId, user);
                    return user;
                });
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateUsers() {
        log.info("{} users needs to be updated ...", users.size());

        users.forEach((employeeId, user) -> {
            log.debug("Updating user: " + employeeId + " with isAdmin: " + user.isAdmin());
            userRepository.mapUserFromResources(employeeId, user.isAdmin()).
                    doOnNext(updatedUser -> {
                        updatedUser.setAdmin(user.isAdmin());
                        users.put(employeeId, updatedUser);
                    });
        });
    }
}
