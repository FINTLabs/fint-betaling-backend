package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.Endpoints;
import no.fint.betaling.exception.InvalidResponseException;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GroupRepository {

    private final RestUtil restUtil;

    private final Endpoints endpoints;

    private final Map<Link, SkoleResource> schools = new HashMap<>();
    private final Map<Link, BasisgruppeResource> basisGroups = new HashMap<>();
    private final Map<Link, UndervisningsgruppeResource> teachingGroups = new HashMap<>();
    private final Map<Link, KontaktlarergruppeResource> contactTeacherGroups = new HashMap<>();
    private final Map<Link, ElevforholdResource> studentRelations = new HashMap<>();
    private final Map<Link, PersonResource> students = new HashMap<>();

    public GroupRepository(RestUtil restUtil, Endpoints endpoints) {
        this.restUtil = restUtil;
        this.endpoints = endpoints;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void init() {
        updateSchools();
        updateBasisGroups();
        updateTeachingGroups();
        updateContactTeacherGroups();
        updateStudentRelations();
        updateStudents();
    }

    @CachePut(value = "schools", unless = "#result == null")
    public Map<Link, SkoleResource> updateSchools() {
        log.info("Updating schools from {} ...", endpoints.getSchool());

        SkoleResources resources;

        try {
            resources = restUtil.get(SkoleResources.class, endpoints.getSchool());
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(resource -> resource.getSelfLinks().forEach(link -> schools.put(link, resource)));

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

    public List<SkoleResource> getDistinctSchools() {
        return getSchools().values().stream().distinct().collect(Collectors.toList());
    }

    @CachePut(value = "basisGroups", unless = "#result == null")
    public Map<Link, BasisgruppeResource> updateBasisGroups() {
        log.info("Updating basis groups from {} ...", endpoints.getBasisGroup());

        BasisgruppeResources resources;

        try {
            resources = restUtil.getUpdates(BasisgruppeResources.class, endpoints.getBasisGroup());
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> r.getSelfLinks().forEach(link -> basisGroups.put(link, r)));

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
        log.info("Updating teaching groups from {} ...", endpoints.getTeachingGroup());

        UndervisningsgruppeResources resources;

        try {
            resources = restUtil.get(UndervisningsgruppeResources.class, endpoints.getTeachingGroup());
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(resource -> resource.getSelfLinks().forEach(link -> teachingGroups.put(link, resource)));

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
        log.info("Updating contact teacher groups from {} ...", endpoints.getContactTeacherGroup());

        KontaktlarergruppeResources resources;

        try {
            resources = restUtil.getUpdates(KontaktlarergruppeResources.class, endpoints.getContactTeacherGroup());
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> r.getSelfLinks().forEach(link -> contactTeacherGroups.put(link, r)));

        log.info("Update completed, {} contact teacher groups.", contactTeacherGroups.size());

        return contactTeacherGroups;
    }

    @Cacheable("contactTeacherGroups")
    public Map<Link, KontaktlarergruppeResource> getContactTeacherGroups() {
        if (contactTeacherGroups.isEmpty()) {
            updateContactTeacherGroups();
        }
        return contactTeacherGroups;
    }

    @CachePut(value = "studentRelations", unless = "#result == null")
    public Map<Link, ElevforholdResource> updateStudentRelations() {
        log.info("Updating student relations from {} ...", endpoints.getStudentRelation());

        ElevforholdResources resources;

        try {
            resources = restUtil.get(ElevforholdResources.class, endpoints.getStudentRelation());
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(r -> r.getSelfLinks().forEach(link -> studentRelations.put(link, r)));

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

    @CachePut(value = "students", unless = "#result == null")
    public Map<Link, PersonResource> updateStudents() {
        log.info("Updating students from {} ...", endpoints.getPerson());

        PersonResources resources;

        try {
            resources = restUtil.get(PersonResources.class, endpoints.getPerson());
        } catch (InvalidResponseException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(person -> person.getElev()
                .forEach(student -> students.put(student, person)));


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

}