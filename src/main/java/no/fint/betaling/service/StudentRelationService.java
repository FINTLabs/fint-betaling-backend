package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentRelationService {

    @Autowired
    private CacheService cacheService;

    @Value("${fint.betaling.endpoints.student-relation}")
    private String studentRelationEndpoint;

    private final Map<String, Map<Link,Link>> studentRelationshipMap = Collections.synchronizedMap(new HashMap<>());

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:500000}")
    public void updateCaches() {
        studentRelationshipMap.forEach(this::updateCache);
    }

    private void updateCache(String orgId, Map<Link,Link> studentRelationship) {
        log.info("{}: Updating cache.", orgId);
        ElevforholdResources elevforholdResources = cacheService.getUpdates(ElevforholdResources.class, studentRelationEndpoint, orgId);
        log.info("{}: Found {} entries", orgId, elevforholdResources.getTotalItems());
        studentRelationship.putAll(elevforholdResources.getContent().stream().collect(Collectors.toMap(this::getSelf, this::getElev)));
        log.info("{}: New size {}", orgId, studentRelationship.size());
    }

    public Map<Link,Link> getStudentRelationships(String orgId) {
        return studentRelationshipMap.computeIfAbsent(orgId, s -> {
           Map<Link,Link> m = Collections.synchronizedMap(new HashMap<>());
           updateCache(orgId, m);
           return m;
        });
    }

    public Link getSelf(ElevforholdResource e) {
        return e.getSelfLinks().stream().findAny().orElseThrow(() -> new IllegalStateException("No self links for " + e));
    }

    public Link getElev(ElevforholdResource e) {
        return e.getElev().stream().findAny().orElseThrow(() -> new IllegalStateException("No elev links for " + e));
    }

}
