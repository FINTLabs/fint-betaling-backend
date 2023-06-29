package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.Endpoints;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GroupRepository {

    private final RestUtil restUtil;

    private final Endpoints endpoints;

    private final Map<Link, SkoleressursResource> schoolresources = new HashMap<>();
    private final ResourceCache<SkoleResource, SkoleResources> schools;
    private final ResourceCache<BasisgruppeResource, BasisgruppeResources> basisGroups;
    private final ResourceCache<UndervisningsgruppeResource, UndervisningsgruppeResources> teachingGroups;
    private final ResourceCache<KontaktlarergruppeResource, KontaktlarergruppeResources> contactTeacherGroups;
    private final ResourceCache<ElevforholdResource, ElevforholdResources> studentRelations;
    private final ResourceCache<PersonResource, PersonResources> students;

    public GroupRepository(RestUtil restUtil, Endpoints endpoints) {
        this.restUtil = restUtil;
        this.endpoints = endpoints;
        schools = new ResourceCache<>(restUtil, endpoints.getSchool(), SkoleResources.class);
        basisGroups = new ResourceCache<>(restUtil, endpoints.getBasisGroup(), BasisgruppeResources.class);
        teachingGroups = new ResourceCache<>(restUtil, endpoints.getTeachingGroup(), UndervisningsgruppeResources.class);
        contactTeacherGroups = new ResourceCache<>(restUtil, endpoints.getContactTeacherGroup(), KontaktlarergruppeResources.class);
        studentRelations = new ResourceCache<>(restUtil, endpoints.getStudentRelation(), ElevforholdResources.class);
        students = new ResourceCache<>(restUtil, endpoints.getPerson(), PersonResources.class);
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:3600000}")
    public void init() {
        schools.update();
        basisGroups.update();
        teachingGroups.update();
        contactTeacherGroups.update();
        studentRelations.update();
        students.update();
        updateSchoolresources();
    }

    public Map<Link, SkoleResource> getSchools() {
        return schools.get();
    }

    public List<SkoleResource> getDistinctSchools() {
        return getSchools().values().stream().distinct().collect(Collectors.toList());
    }

    public Map<Link, BasisgruppeResource> getBasisGroups() {
     return basisGroups.get();
    }

    public Map<Link, UndervisningsgruppeResource> getTeachingGroups() {
        return teachingGroups.get();
    }

    public Map<Link, KontaktlarergruppeResource> getContactTeacherGroups() {
        return contactTeacherGroups.get();
    }

    public Map<Link, ElevforholdResource> getStudentRelations() {
        return studentRelations.get();
    }

    public Map<Link, PersonResource> updateStudents() {
        log.info("Updating students from {} ...", endpoints.getPerson());

        PersonResources resources;

        try {
            resources = restUtil.get(PersonResources.class, endpoints.getPerson()).block();
        } catch (WebClientResponseException ex) {
            log.error(ex.getMessage());
            return null;
        }

        if (resources.getTotalItems() == 0) return null;

        resources.getContent().forEach(person -> person.getElev()
                .forEach(student -> students.put(student, person)));


        log.info("Update completed, {} students.", students.size());

        return students;
    }

    public Map<Link, PersonResource> getStudents() {
        return students.get();
    }

    public Map<Link, PersonResource> updateSchoolresources() {
        log.info("Updating skoleressurs from {} ...", endpoints.getSchoolResource());

        SkoleressursResources resources;

        try {
            resources = restUtil.get(SkoleressursResources.class, endpoints.getSchoolResource()).block();
        } catch (WebClientResponseException ex) {
            log.error(ex.getMessage());
            return null;
        }

        if (resources.getTotalItems() == 0) return null;


        resources.getContent().forEach(r -> r.getPersonalressurs().forEach(link -> schoolresources.put(link, r)));
        log.info("Update completed, {} schoolresources.", schoolresources.size());

        return students;
    }

    public Map<Link, SkoleressursResource> getSchoolresources() {
        if (schoolresources.isEmpty()) {
            updateSchoolresources();
        }
        return schoolresources;
    }

}