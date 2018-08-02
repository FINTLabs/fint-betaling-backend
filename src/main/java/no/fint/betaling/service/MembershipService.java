package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.ResourceCache;
import no.fint.model.resource.utdanning.elev.MedlemskapResource;
import no.fint.model.resource.utdanning.elev.MedlemskapResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class MembershipService {

    @Autowired
    private CacheService cacheService;

    @Value("${fint.betaling.endpoints.membership}")
    private String membershipEndpoint;

    private ResourceCache<MedlemskapResource> medlemskapResourceResourceCache;

    @PostConstruct
    public void init() { medlemskapResourceResourceCache = new ResourceCache<>(cacheService, membershipEndpoint, MedlemskapResources.class); }

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:360000}")
    public void updateCaches() {
        medlemskapResourceResourceCache.updateCaches();
    }

    public List<MedlemskapResource> getMemberships(String orgId) {
        return medlemskapResourceResourceCache.getResources(orgId);
    }

}
