package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.utdanning.elev.MedlemskapResource;
import no.fint.model.resource.utdanning.elev.MedlemskapResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MembershipService {

    @Autowired
    private CacheService cacheService;

    @Value("${fint.betaling.endpoints.membership}")
    private String membershipEndpoint;

    private final Map<String, List<MedlemskapResource>> membershipCache = Collections.synchronizedMap(new HashMap<>());

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:500000}")
    public void updateCaches() {
        membershipCache.forEach(this::updateCache);
    }

    private void updateCache(String orgId, List<MedlemskapResource> membershipList) {
        log.info("{}: Updating cache.", orgId);
        MedlemskapResources medlemskapResources = cacheService.getUpdates(MedlemskapResources.class, membershipEndpoint, orgId);
        log.info("{}: Found {} entries", orgId, medlemskapResources.getTotalItems());
        membershipList.addAll(medlemskapResources.getContent());
        log.info("{}: New size {}", orgId, membershipList.size());
    }

    public List<MedlemskapResource> getMemberships(String orgId) {
        return membershipCache.computeIfAbsent(orgId, s -> {
           List<MedlemskapResource> l = Collections.synchronizedList(new ArrayList<>());
           updateCache(orgId, l);
           return l;
        });
    }

}
