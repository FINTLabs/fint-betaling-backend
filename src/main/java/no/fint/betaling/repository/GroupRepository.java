package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InvalidResponseException;
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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.*;

@Slf4j
@Repository
public class GroupRepository {

    private final RestUtil restUtil;

    private final Map<Link, SkoleResource> schools = new HashMap<>();
    private final Map<Link, BasisgruppeResource> basisGroups = new HashMap<>();
    private final Map<Link, UndervisningsgruppeResource> teachingGroups = new HashMap<>();
    private final Map<Link, KontaktlarergruppeResource> contactTeacherGroups = new HashMap<>();
    private final Map<Link, PersonResource> students = new HashMap<>();
    private final Map<Link, ElevforholdResource> studentRelations = new HashMap<>();

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

    public GroupRepository(RestUtil restUtil) {
        this.restUtil = restUtil;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void init() {
        updateSchools();
        updateBasisGroups();
        updateTeachingGroups();
        updateContactTeacherGroups();
        updateStudents();
        updateStudentRelations();
    }

    @CachePut(value = "schools", unless = "#result == null")
    public Map<Link, SkoleResource> updateSchools() {
        log.info("Updating schools from {} ...", schoolEndpoint);

        SkoleResources resources;

        try {
            resources = restUtil.getUpdates(SkoleResources.class, schoolEndpoint);
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(resource -> schools.put(getSelfLink(resource), resource));

        log.info("Update completed, {} schools.", schools.size());

        return schools;
    }

    @Cacheable("schools")
    public Map<Link, SkoleResource> getSchools() {
        if (schools.isEmpty()) {
            updateSchools();
        }
        return schools;
    }

    @CachePut(value = "basisGroups", unless = "#result == null")
    public Map<Link, BasisgruppeResource> updateBasisGroups() {
        log.info("Updating basis groups from {} ...", basisGroupEndpoint);

        BasisgruppeResources resources;

        try {
            resources = restUtil.getUpdates(BasisgruppeResources.class, basisGroupEndpoint);
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> basisGroups.put(getSelfLink(r), r));

        log.info("Update completed, {} basis groups.", basisGroups.size());

        return basisGroups;
    }

    @Cacheable("basisGroups")
    public Map<Link, BasisgruppeResource> getBasisGroups() {
        if (basisGroups.isEmpty()) {
            updateBasisGroups();
        }
        return basisGroups;
    }

    @CachePut(value = "teachingGroups", unless = "#result == null")
    public Map<Link, UndervisningsgruppeResource> updateTeachingGroups() {
        log.info("Updating teaching groups from {} ...", teachingGroupEndpoint);

        UndervisningsgruppeResources resources;

        try {
            resources = restUtil.getUpdates(UndervisningsgruppeResources.class, teachingGroupEndpoint);
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(resource -> teachingGroups.put(getSelfLink(resource), resource));

        log.info("Update completed, {} teaching groups.", teachingGroups.size());

        return teachingGroups;
    }

    @Cacheable("teachingGroups")
    public Map<Link, UndervisningsgruppeResource> getTeachingGroups() {
        if (teachingGroups.isEmpty()) {
            updateTeachingGroups();
        }
        return teachingGroups;
    }

    @CachePut(value = "contactTeacherGroups", unless = "#result == null")
    public Map<Link, KontaktlarergruppeResource> updateContactTeacherGroups() {
        log.info("Updating contact teacher groups from {} ...", contactTeacherGroupEndpoint);

        KontaktlarergruppeResources resources;

        try {
            resources = restUtil.getUpdates(KontaktlarergruppeResources.class, contactTeacherGroupEndpoint);
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> contactTeacherGroups.put(getSelfLink(r), r));

        log.info("Update completed, {} contact teacher groups.", contactTeacherGroups.size());

        return contactTeacherGroups;
    }

    @Cacheable("contactTeacherGoups")
    public Map<Link, KontaktlarergruppeResource> getContactTeacherGroups() {
        if (contactTeacherGroups.isEmpty()) {
            updateContactTeacherGroups();
        }
        return contactTeacherGroups;
    }

    @CachePut(value = "students", unless = "#result == null")
    public Map<Link, PersonResource> updateStudents() {
        log.info("Updating students from {} ...", personEndpoint);

        PersonResources resources;

        try {
            resources = restUtil.getUpdates(PersonResources.class, personEndpoint);
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> students.put(getStudentLink(r), r));

        log.info("Update completed, {} students.", students.size());

        return students;
    }

    @Cacheable("students")
    public Map<Link, PersonResource> getStudents() {
        if (students.isEmpty()) {
            updateStudents();
        }
        return students;
    }

    @CachePut(value = "studentRelations", unless = "#result == null")
    public Map<Link, ElevforholdResource> updateStudentRelations() {
        log.info("Updating student relations from {} ...", studentRelationEndpoint);

        ElevforholdResources resources;

        try {
            resources = restUtil.getUpdates(ElevforholdResources.class, studentRelationEndpoint);
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> studentRelations.put(getSelfLink(r), r));

        log.info("Update completed, {} student relations.", studentRelations.size());

        return studentRelations;
    }

    @Cacheable("studentRelations")
    public Map<Link, ElevforholdResource> getStudentRelations() {
        if (studentRelations.isEmpty()) {
            updateStudentRelations();
        }
        return studentRelations;
    }

    private <T extends FintLinks> Link getSelfLink(T resource) {
        return resource.getSelfLinks().stream().findFirst().orElse(null);
    }

    private Link getStudentLink(PersonResource resource) {
        return resource.getElev().stream().findFirst().orElse(null);
    }
}