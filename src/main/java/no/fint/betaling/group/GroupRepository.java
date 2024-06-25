package no.fint.betaling.group;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.fintdata.ResourceCache;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Slf4j
@Repository
public class GroupRepository {

    private final ResourceCache<BasisgruppeResource, BasisgruppeResources> basisGroups;
    private final ResourceCache<KontaktlarergruppeResource, KontaktlarergruppeResources> contactTeacherGroups;
    private final ResourceCache<ElevforholdResource, ElevforholdResources> studentRelations;
    private final ResourceCache<PersonResource, PersonResources> students;

    public GroupRepository(RestUtil restUtil, Endpoints endpoints) {
        basisGroups = new ResourceCache<>(restUtil, endpoints.getBasisGroup(), BasisgruppeResources.class);
        contactTeacherGroups = new ResourceCache<>(restUtil, endpoints.getContactTeacherGroup(), KontaktlarergruppeResources.class);
        studentRelations = new ResourceCache<>(restUtil, endpoints.getStudentRelation(), ElevforholdResources.class);
        students = new ResourceCache<>(restUtil, endpoints.getPerson(), PersonResources.class, PersonResource::getElev);
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:3600000}")
    public void updateAll() {
        int basisGroupsUpdated = basisGroups.update();
        int contactTeachersUpdated = contactTeacherGroups.update();
        int studentRelationsUpdated = studentRelations.update();
        int studentsUpdated = students.update();
        log.info("Updates completed, basisgruppe ({}), undervisningsgruppe ({}), elevforhold ({}), elev ({})", basisGroupsUpdated, contactTeachersUpdated, studentRelationsUpdated, studentsUpdated);
    }

    public Map<Link, BasisgruppeResource> getBasisGroups() {
        return basisGroups.get();
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

}