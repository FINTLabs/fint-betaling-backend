package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
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
public class CacheService {

    private final RestUtil restUtil;
    private final CacheManager cacheManager;

    public CacheService(RestUtil restUtil, CacheManager cacheManager) {
        this.restUtil = restUtil;
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

    @Scheduled(initialDelay = 1000, fixedRate = 3600000)
    public void init() {
        for (String orgId : orgs) {
            Instant start = Instant.now();
            log.info("{}: updating caches...", orgId);
            updateSchoolCache(orgId);
            updateBasisGroupCache(orgId);
            updateTeachingGroupCache(orgId);
            updateContactTeacherGroupCache(orgId);
            updateStudentCache(orgId);
            updateStudentRelationCache(orgId);
            Instant finish = Instant.now();
            log.info("{}: finished updating caches after {} milliseconds", orgId, Duration.between(start, finish).toMillis());
        }
    }

    private void updateSchoolCache(String orgId) {
        Cache cache = cacheManager.getCache("schoolCache");

        SkoleResources resources = restUtil.get(SkoleResources.class, skoleEndpoint, orgId);

        if (resources != null) {
            Map<String, SkoleResource> schools = resources.getContent().stream()
                    .collect(Collectors.toMap(this::getOrganizationNumber, Function.identity(), (a, b) -> a));
            cache.put(orgId, schools);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateBasisGroupCache(String orgId) {
        Cache cache = cacheManager.getCache("basisGroupCache");

        BasisgruppeResources resources = restUtil.get(BasisgruppeResources.class, basisgruppeEndpoint, orgId);

        if (resources != null) {
            Map<Link, List<BasisgruppeResource>> basisGroups = resources.getContent().stream()
                    .collect(Collectors.groupingBy(this::getSchoolLink));
            cache.put(orgId, basisGroups);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateTeachingGroupCache(String orgId) {
        Cache cache = cacheManager.getCache("teachingGroupCache");

        UndervisningsgruppeResources resources = restUtil.get(UndervisningsgruppeResources.class, undervisningsgruppeEndpoint, orgId);

        if (resources != null) {
            Map<Link, List<UndervisningsgruppeResource>> teachingGroups = resources.getContent().stream()
                    .collect(Collectors.groupingBy(this::getSchoolLink));
            cache.put(orgId, teachingGroups);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateContactTeacherGroupCache(String orgId) {
        Cache cache = cacheManager.getCache("contactTeacherGroupCache");

        KontaktlarergruppeResources resources = restUtil.get(KontaktlarergruppeResources.class, kontaktlarergruppeEndpoint, orgId);

        if (resources != null) {
            Map<Link, List<KontaktlarergruppeResource>> contactTeacherGroups = resources.getContent().stream()
                    .collect(Collectors.groupingBy(this::getSchoolLink));
            cache.put(orgId, contactTeacherGroups);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateStudentCache(String orgId) {
        Cache cache = cacheManager.getCache("studentCache");

        PersonResources resources = restUtil.get(PersonResources.class, personEndpoint, orgId);

        if (resources != null) {
            Map<Link, PersonResource> students = resources.getContent().stream()
                    .collect(Collectors.toMap(this::getStudentLink, Function.identity(), (a, b) -> a));
            cache.put(orgId, students);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateStudentRelationCache(String orgId) {
        Cache cache = cacheManager.getCache("studentRelationCache");

        ElevforholdResources resources = restUtil.get(ElevforholdResources.class, elevforholdEndpoint, orgId);

        if (resources != null) {
            Map<Link, ElevforholdResource> studentRelations = resources.getContent().stream()
                    .collect(Collectors.toMap(this::getSelfLink, Function.identity(), (a, b) -> a));
            cache.put(orgId, studentRelations);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private<T extends FintLinks> Link getSelfLink(T resource) {
        return resource.getSelfLinks().stream().findAny().orElse(null);
    }

    private<T extends FintLinks> Link getSchoolLink(T resource) {
        return resource.getLinks().get("skole").stream().findAny().orElse(null);
    }

    private Link getStudentLink(PersonResource resource) {
        return resource.getElev().stream().findAny().orElse(null);
    }

    private String getOrganizationNumber(SkoleResource resource) {
        return resource.getOrganisasjonsnummer().getIdentifikatorverdi();
    }
}