package no.fint.betaling.group;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.ResourceCache;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GroupRepository {

    private final ResourceCache<SkoleResource, SkoleResources> schools;
    private final ResourceCache<BasisgruppeResource, BasisgruppeResources> basisGroups;
    private final ResourceCache<UndervisningsgruppeResource, UndervisningsgruppeResources> teachingGroups;
    private final ResourceCache<KontaktlarergruppeResource, KontaktlarergruppeResources> contactTeacherGroups;
    private final ResourceCache<ElevforholdResource, ElevforholdResources> studentRelations;
    private final ResourceCache<PersonResource, PersonResources> students;
    private final ResourceCache<SkoleressursResource, SkoleressursResources> schoolresources;

    public GroupRepository(RestUtil restUtil, Endpoints endpoints) {
        schools = new ResourceCache<>(restUtil, endpoints.getSchool(), SkoleResources.class);
        basisGroups = new ResourceCache<>(restUtil, endpoints.getBasisGroup(), BasisgruppeResources.class);
        teachingGroups = new ResourceCache<>(restUtil, endpoints.getTeachingGroup(), UndervisningsgruppeResources.class);
        contactTeacherGroups = new ResourceCache<>(restUtil, endpoints.getContactTeacherGroup(), KontaktlarergruppeResources.class);
        studentRelations = new ResourceCache<>(restUtil, endpoints.getStudentRelation(), ElevforholdResources.class);
        students = new ResourceCache<>(restUtil, endpoints.getPerson(), PersonResources.class, PersonResource::getElev);
        schoolresources = new ResourceCache<>(restUtil, endpoints.getSchoolResource(), SkoleressursResources.class, SkoleressursResource::getPersonalressurs);
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:3600000}")
    public void updateAll() {
        int schoolsUpdated = schools.update();
        int basisGroupsUpdated = basisGroups.update();
        int teachingGroupsUpdated = teachingGroups.update();
        int contactTeachersUpdated = contactTeacherGroups.update();
        int studentRelationsUpdated = studentRelations.update();
        int studentsUpdated = students.update();
        int schoolResourceUpdated = schoolresources.update();
        log.info("Updates completed, skole ({}), basisgruppe ({}), undervisningsgruppe ({}), kontaktlarergruppe ({}), elevforhold ({}), elev ({}), skoleressurs ({})", schoolsUpdated, basisGroupsUpdated, teachingGroupsUpdated, contactTeachersUpdated, studentRelationsUpdated, studentsUpdated, schoolResourceUpdated);
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

    public Map<Link, PersonResource> getStudents() {
        return students.get();
    }

    public Map<Link, SkoleressursResource> getSchoolresources() {
        return schoolresources.get();
    }
}