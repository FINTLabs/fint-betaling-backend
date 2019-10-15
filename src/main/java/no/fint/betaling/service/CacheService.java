package no.fint.betaling.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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
            buildStudentRelationsMap(orgId);
            buildStudentsMap(orgId);
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

    private void buildStudentRelationsMap(String orgId) {
        Cache cache = cacheManager.getCache("studentRelations");
        ElevforholdResources resources = cache.get(orgId, ElevforholdResources.class);

        Map<Link, ElevforholdResource> map = resources.getContent().stream()
                .collect(Collectors.toMap(this::getSelfLink, Function.identity(), (a, b) -> a));

        Cache cacheMap = cacheManager.getCache("studentRelationsMap");
        cacheMap.put(orgId, map);
    }

    private void buildStudentsMap(String orgId) {
        Cache cache = cacheManager.getCache("persons");
        PersonResources resources = cache.get(orgId, PersonResources.class);

        Map<Link, PersonResource> map = resources.getContent().stream()
                .collect(Collectors.toMap(this::getStudentLink, Function.identity(), (a, b) -> a));

        Cache cacheMap = cacheManager.getCache("studentsMap");
        cacheMap.put(orgId, map);
    }

    private Link getSelfLink(ElevforholdResource resource) {
        return resource.getSelfLinks().stream().findAny().orElse(null);
    }

    private Link getStudentLink(PersonResource resource) {
        return resource.getElev().stream().findAny().orElse(null);
    }
}