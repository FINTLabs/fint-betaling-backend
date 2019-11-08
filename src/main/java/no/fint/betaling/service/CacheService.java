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

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Value("${fint.betaling.endpoints.school}")
    private URI schoolEndpoint;

    @Value("${fint.betaling.endpoints.basis-group}")
    private URI basisGroupEndpoint;

    @Value("${fint.betaling.endpoints.teaching-group}")
    private URI teachingGroupEndpoint;

    @Value("${fint.betaling.endpoints.contact-teacher-group}")
    private URI contactTeacherGroupEndpoint;

    @Value("${fint.betaling.endpoints.person}")
    private URI personEndpoint;

    @Value("${fint.betaling.endpoints.student-relation}")
    private URI studentRelationEndpoint;

    @Value("${fint.betaling.endpoints.school-resource}")
    private URI schoolResourceEndpoint;

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
            //updateSchoolResourceCache(orgId);
            Instant finish = Instant.now();
            log.info("{}: finished updating caches after {} milliseconds", orgId, Duration.between(start, finish).toMillis());
        }
    }

    private void updateSchoolCache(String orgId) {
        Cache cache = cacheManager.getCache("schoolCache");

        SkoleResources resources = restUtil.get(SkoleResources.class, schoolEndpoint, orgId);

        if (resources != null) {
            Map<String, SkoleResource> schools = resources.getContent().stream()
                    .collect(Collectors.toMap(this::getOrganizationNumber, Function.identity(), (a, b) -> a));
            cache.put(orgId, schools);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateBasisGroupCache(String orgId) {
        Cache cache = cacheManager.getCache("basisGroupCache");

        BasisgruppeResources resources = restUtil.get(BasisgruppeResources.class, basisGroupEndpoint, orgId);

        if (resources != null) {
            Map<Link, List<BasisgruppeResource>> basisGroups = resources.getContent().stream()
                    .collect(Collectors.groupingBy(this::getSchoolLink));
            cache.put(orgId, basisGroups);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateTeachingGroupCache(String orgId) {
        Cache cache = cacheManager.getCache("teachingGroupCache");

        UndervisningsgruppeResources resources = restUtil.get(UndervisningsgruppeResources.class, teachingGroupEndpoint, orgId);

        if (resources != null) {
            Map<Link, List<UndervisningsgruppeResource>> teachingGroups = resources.getContent().stream()
                    .collect(Collectors.groupingBy(this::getSchoolLink));
            cache.put(orgId, teachingGroups);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    private void updateContactTeacherGroupCache(String orgId) {
        Cache cache = cacheManager.getCache("contactTeacherGroupCache");

        KontaktlarergruppeResources resources = restUtil.get(KontaktlarergruppeResources.class, contactTeacherGroupEndpoint, orgId);

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

        ElevforholdResources resources = restUtil.get(ElevforholdResources.class, studentRelationEndpoint, orgId);

        if (resources != null) {
            Map<Link, ElevforholdResource> studentRelations = resources.getContent().stream()
                    .collect(Collectors.toMap(this::getSelfLink, Function.identity(), (a, b) -> a));
            cache.put(orgId, studentRelations);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }

    /*
    private void updateSchoolResourceCache(String orgId) {
        Cache cache = cacheManager.getCache("schoolResourceCache");

        SkoleressursResources resources = restUtil.get(SkoleressursResources.class, skoleressursEndpoint, orgId);

        if (resources != null) {
            Map<String, SkoleressursResource> schoolResources = resources.getContent().stream()
                    .collect(Collectors.toMap(this::getFeideName, Function.identity(), (a, b) -> a));
            cache.put(orgId, schoolResources);
        }

        cache.putIfAbsent(orgId, Collections.emptyMap());
    }
     */

    private String getOrganizationNumber(SkoleResource resource) {
        if (resource.getOrganisasjonsnummer() == null) return null;

        return resource.getOrganisasjonsnummer().getIdentifikatorverdi();
    }

    private <T extends FintLinks> Link getSchoolLink(T resource) {
        return resource.getLinks().get("skole").stream().findAny().orElse(null);
    }

    private Link getStudentLink(PersonResource resource) {
        return resource.getElev().stream().findAny().orElse(null);
    }

    private Link getSelfLink(ElevforholdResource resource) {
        return resource.getSelfLinks().stream().findAny().orElse(null);
    }

    /*
    private String getFeideName(SkoleressursResource resource) {
        if (resource.getFeidenavn() == null) return null;

        return resource.getFeidenavn().getIdentifikatorverdi();
    }
     */
}