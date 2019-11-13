package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.RestUtil;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
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
        updateSchoolCache();
        updateBasisGroupCache();
        updateTeachingGroupCache();
        updateContactTeacherGroupCache();
        updateStudentCache();
        updateStudentRelationCache();
        //updateSchoolResourceCache(orgId);
        log.info("Finished updating caches");
    }

    private void updateSchoolCache() {
        Cache cache = cacheManager.getCache("fintCache");

        SkoleResources resources = restUtil.get(SkoleResources.class, schoolEndpoint);

        if (resources == null) {
            cache.putIfAbsent("schools", Collections.emptyMap());
            return;
        }

        Map<String, SkoleResource> schools = resources.getContent().stream()
                .collect(Collectors.toMap(this::getOrganizationNumber, Function.identity(), (a, b) -> a));

        cache.put("schools", schools);
    }

    private void updateBasisGroupCache() {
        Cache cache = cacheManager.getCache("fintCache");

        BasisgruppeResources resources = restUtil.get(BasisgruppeResources.class, basisGroupEndpoint);

        if (resources == null) {
            cache.putIfAbsent("basisGroups", Collections.emptyMap());
            return;
        }

        Map<Link, List<BasisgruppeResource>> basisGroups = resources.getContent().stream()
                .collect(Collectors.groupingBy(this::getSchoolLink));

        cache.put("basisGroups", basisGroups);
    }

    private void updateTeachingGroupCache() {
        Cache cache = cacheManager.getCache("fintCache");

        UndervisningsgruppeResources resources = restUtil.get(UndervisningsgruppeResources.class, teachingGroupEndpoint);

        if (resources == null) {
            cache.putIfAbsent("teachingGroups", Collections.emptyMap());
            return;
        }

        Map<Link, List<UndervisningsgruppeResource>> teachingGroups = resources.getContent().stream()
                .collect(Collectors.groupingBy(this::getSchoolLink));

        cache.put("teachingGroups", teachingGroups);
    }

    private void updateContactTeacherGroupCache() {
        Cache cache = cacheManager.getCache("fintCache");

        KontaktlarergruppeResources resources = restUtil.get(KontaktlarergruppeResources.class, contactTeacherGroupEndpoint);

        if (resources == null) {
            cache.putIfAbsent("contactTeacherGroups", Collections.emptyMap());
            return;
        }

        Map<Link, List<KontaktlarergruppeResource>> contactTeacherGroups = resources.getContent().stream()
                .collect(Collectors.groupingBy(this::getSchoolLink));

        cache.put("contactTeacherGroups", contactTeacherGroups);
    }

    private void updateStudentCache() {
        Cache cache = cacheManager.getCache("fintCache");

        PersonResources resources = restUtil.get(PersonResources.class, personEndpoint);

        if (resources == null) {
            cache.putIfAbsent("students", Collections.emptyMap());
            return;
        }

        Map<Link, PersonResource> students = resources.getContent().stream()
                .collect(Collectors.toMap(this::getStudentLink, Function.identity(), (a, b) -> a));

        cache.put("students", students);
    }

    private void updateStudentRelationCache() {
        Cache cache = cacheManager.getCache("fintCache");

        ElevforholdResources resources = restUtil.get(ElevforholdResources.class, studentRelationEndpoint);

        if (resources == null) {
            cache.putIfAbsent("studentRelations", Collections.emptyMap());
            return;
        }

        Map<Link, ElevforholdResource> studentRelations = resources.getContent().stream()
                .collect(Collectors.toMap(this::getSelfLink, Function.identity(), (a, b) -> a));

        cache.put("studentRelations", studentRelations);
    }

    private String getOrganizationNumber(SkoleResource resource) {
        Identifikator organisationNumber = resource.getOrganisasjonsnummer();

        return (organisationNumber != null ? organisationNumber.getIdentifikatorverdi() : null);
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

    public Map getResources(String name) {
        Cache cache = cacheManager.getCache("fintCache");

        return cache.get(name, Map.class);
    }

    /*
    private void updateSchoolResourceCache() {
        Cache cache = cacheManager.getCache("fintCache");

        SkoleressursResources resources = restUtil.get(SkoleressursResources.class, schoolResourceEndpoint);

        if (resources == null) {
            cache.putIfAbsent("schoolResources", Collections.emptyMap());
            return;
        }

        Map<String, SkoleressursResource> schoolResources = resources.getContent().stream()
                .collect(Collectors.toMap(this::getFeideName, Function.identity(), (a, b) -> a));

        cache.put("schoolResources", schoolResources);
    }

    private String getFeideName(SkoleressursResource resource) {
        Identifikator feideName = resource.getFeidenavn();

        return (feideName != null ? feideName.getIdentifikatorverdi() : null);
    }
     */
}