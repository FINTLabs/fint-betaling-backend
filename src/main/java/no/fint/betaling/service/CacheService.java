package no.fint.betaling.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.util.RestUtil;
import no.fint.model.FintMainObject;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import no.fint.model.utdanning.utdanningsprogram.Skole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Getter
public class CacheService {
    private final RestUtil restUtil;
    private final KundeFactory kundeFactory;
    private final CacheManager cacheManager;

    public CacheService(RestUtil restUtil, KundeFactory kundeFactory, CacheManager cacheManager) {
        this.restUtil = restUtil;
        this.kundeFactory = kundeFactory;
        this.cacheManager = cacheManager;
    }

    @Value("${fint.betaling.org-ids}")
    private List<String> orgs;

    @Value("${fint.betaling.endpoints.skole}")
    private String skoleEndpoint;

    @Value("${fint.betaling.endpoints.basisgruppe}")
    private String basisgruppeEndpoint;

    @Value("${fint.betaling.endpoints.undervisningsgruppe}")
    private String undervisningsgruppeEndpoint;

    @Value("${fint.betaling.endpoints.kontaktlarergruppe}")
    private String kontaktlarergruppeEndpoint;

    @Value("${fint.betaling.endpoints.person}")
    private String personEndpoint;

    @Value("${fint.betaling.endpoints.elevforhold}")
    private String elevforholdEndpoint;

    @Scheduled(initialDelay = 10000, fixedRate = 3600000)
    public void init() {
        log.info("Updating caches...");
        Instant start = Instant.now();
        for (String orgId : orgs) {
            updateSchools(orgId);
            updateBasisGroups(orgId);
            updateTeachingGroups(orgId);
            updateContactTeacherGroups(orgId);
            updatePersons(orgId);
            updateStudentRelations(orgId);
            buildCustomerBase(orgId);
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        log.info("Finished updating caches after {} milliseconds", timeElapsed);
    }

    private void updateSchools(String orgId) {
        Cache cache = cacheManager.getCache("schools");
        SkoleResources resources = cache.get(orgId, SkoleResources.class);

        SkoleResources updates = restUtil.getUpdates(SkoleResources.class, skoleEndpoint, orgId);

        if (resources == null) {
            cache.put(orgId, updates);
        } else if (updates.getTotalItems() > 0) {
            updates.getContent().forEach(resources::addResource);
            cache.put(orgId, resources);
        }
    }

    private void updateBasisGroups(String orgId) {
        Cache cache = cacheManager.getCache("basisGroups");
        BasisgruppeResources resources = cache.get(orgId, BasisgruppeResources.class);

        BasisgruppeResources updates = restUtil.getUpdates(BasisgruppeResources.class, basisgruppeEndpoint, orgId);

        if (resources == null) {
            cache.put(orgId, updates);
        } else if (updates.getTotalItems() > 0) {
            updates.getContent().forEach(resources::addResource);
            cache.put(orgId, resources);
        }
    }

    private void updateTeachingGroups(String orgId) {
        Cache cache = cacheManager.getCache("teachingGroups");
        UndervisningsgruppeResources resources = cache.get(orgId, UndervisningsgruppeResources.class);

        UndervisningsgruppeResources updates = restUtil.getUpdates(UndervisningsgruppeResources.class, undervisningsgruppeEndpoint, orgId);

        if (resources == null) {
            cache.put(orgId, updates);
        } else if (updates.getTotalItems() > 0) {
            updates.getContent().forEach(resources::addResource);
            cache.put(orgId, resources);
        }
    }

    private void updateContactTeacherGroups(String orgId) {
        Cache cache = cacheManager.getCache("contactTeacherGroups");
        KontaktlarergruppeResources resources = cache.get(orgId, KontaktlarergruppeResources.class);

        KontaktlarergruppeResources updates = restUtil.getUpdates(KontaktlarergruppeResources.class, kontaktlarergruppeEndpoint, orgId);

        if (resources == null) {
            cache.put(orgId, updates);
        } else if (updates.getTotalItems() > 0) {
            updates.getContent().forEach(resources::addResource);
            cache.put(orgId, resources);
        }
    }

    private void updatePersons(String orgId) {
        Cache cache = cacheManager.getCache("persons");
        PersonResources resources = cache.get(orgId, PersonResources.class);

        PersonResources updates = restUtil.getUpdates(PersonResources.class, personEndpoint, orgId);

        if (resources == null) {
            cache.put(orgId, updates);
        } else if (updates.getTotalItems() > 0) {
            updates.getContent().forEach(resources::addResource);
            cache.put(orgId, resources);
        }
    }

    private void updateStudentRelations(String orgId) {
        Cache cache = cacheManager.getCache("studentRelations");
        ElevforholdResources resources = cache.get(orgId, ElevforholdResources.class);

        ElevforholdResources updates = restUtil.getUpdates(ElevforholdResources.class, elevforholdEndpoint, orgId);

        if (resources == null) {
            cache.put(orgId, updates);
        } else if (updates.getTotalItems() > 0) {
            updates.getContent().forEach(resources::addResource);
            cache.put(orgId, resources);
        }
    }

    private void buildCustomerBase(String orgId) {
        Cache studentRelationCache = cacheManager.getCache("studentRelations");
        ElevforholdResources studentRelations = studentRelationCache.get(orgId, ElevforholdResources.class);

        Cache personCache = cacheManager.getCache("persons");
        PersonResources persons = personCache.get(orgId, PersonResources.class);

        Map<Link, Kunde> customerMap = studentRelations.getContent()
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getSelfLinks().stream().findAny().orElseGet(Link::new),
                        s -> {
                            Link student = s.getElev().stream().findAny().orElseGet(Link::new);
                            return persons.getContent().stream()
                                    .filter(p -> p.getElev().contains(student))
                                    .findAny()
                                    .map(kundeFactory::getKunde)
                                    .orElseGet(Kunde::new);
                        }));

        cacheManager.getCache("customers").put(orgId, customerMap);
    }
}