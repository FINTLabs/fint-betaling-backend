package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.ResourceNotFoundException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class GroupService {

    private final CacheManager cacheManager;

    public GroupService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private SkoleResource getSchool(String orgId, String schoolId) {
        Cache cache = cacheManager.getCache("schoolCache");
        Map<String, SkoleResource> schools = (Map<String, SkoleResource>) cache.get(orgId).get();

        SkoleResource school = schools.get(schoolId);

        if (school == null) throw new ResourceNotFoundException();

        return school;
    }

    public KundeGruppe getCustomerGroupBySchool(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        return createCustomerGroup(orgId, school.getNavn(), null, school.getElevforhold());
    }

    public List<KundeGruppe> getCustomerGroupsByBasisGroup(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Link schoolLink = getSelfLink(school);

        Cache cache = cacheManager.getCache("basisGroupCache");
        Map<Link, List<BasisgruppeResource>> resources = (Map<Link, List<BasisgruppeResource>>) cache.get(orgId).get();

        List<BasisgruppeResource> basisGroups = resources.get(schoolLink);

        return basisGroups.stream()
                .map(b -> createCustomerGroup(orgId, b.getNavn(), b.getBeskrivelse(), b.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupsByTeachingGroup(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Link schoolLink = getSelfLink(school);

        Cache cache = cacheManager.getCache("teachingGroupCache");
        Map<Link, List<UndervisningsgruppeResource>> resources = (Map<Link, List<UndervisningsgruppeResource>>) cache.get(orgId).get();

        List<UndervisningsgruppeResource> teachingGroups = resources.get(schoolLink);

        return teachingGroups.stream()
                .map(t -> createCustomerGroup(orgId, t.getNavn(), t.getBeskrivelse(), t.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupsByContactTeacherGroup(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Link schoolLink = getSelfLink(school);

        Cache cache = cacheManager.getCache("contactTeacherGroupCache");
        Map<Link, List<KontaktlarergruppeResource>> resources = (Map<Link, List<KontaktlarergruppeResource>>) cache.get(orgId).get();

        List<KontaktlarergruppeResource> contactTeacherGroups = resources.get(schoolLink);

        return contactTeacherGroups.stream()
                .map(c -> createCustomerGroup(orgId, c.getNavn(), c.getBeskrivelse(), c.getElevforhold()))
                .collect(Collectors.toList());
    }

    private KundeGruppe createCustomerGroup(String orgId, String navn, String beskrivelse, List<Link> elevforhold) {
        KundeGruppe kundeGruppe = new KundeGruppe();
        kundeGruppe.setNavn(navn);
        kundeGruppe.setBeskrivelse(beskrivelse);
        kundeGruppe.setKundeliste(getCustomersForGroup(orgId, elevforhold));
        return kundeGruppe;
    }

    private List<Kunde> getCustomersForGroup(String orgId, List<Link> elevforhold) {
        Cache studentCache = cacheManager.getCache("studentCache");
        Map<Link, PersonResource> students = (Map<Link, PersonResource>) studentCache.get(orgId).get();

        Cache studentRelationCache = cacheManager.getCache("studentRelationCache");
        Map<Link, ElevforholdResource> studentRelations = (Map<Link, ElevforholdResource>) studentRelationCache.get(orgId).get();

        return elevforhold.stream()
                .map(studentRelations::get)
                .map(this::getStudentLink)
                .filter(Objects::nonNull)
                .map(students::get)
                .map(Kunde::of)
                .collect(Collectors.toList());
    }

    private Link getStudentLink(ElevforholdResource resource) {
        return resource.getElev().stream().findAny().orElse(null);
    }

    private<T extends FintLinks> Link getSelfLink(T resource) {
        return resource.getSelfLinks().stream().findAny().orElse(null);
    }
}