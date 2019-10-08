package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.ResourceNotFoundException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {
    private final CacheManager cacheManager;

    public GroupService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private SkoleResource getSchool(String orgId, String schoolId) {
        Cache fintCache = cacheManager.getCache("schools");
        SkoleResources resources = fintCache.get(orgId, SkoleResources.class);

        return resources.getContent().stream()
                .filter(s -> s.getOrganisasjonsnummer().getIdentifikatorverdi().equals(schoolId))
                .findAny().orElseThrow(ResourceNotFoundException::new);
    }

    public KundeGruppe getCustomerGroupBySchool(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        return createCustomerGroup(orgId, school.getNavn(), null, school.getElevforhold());
    }

    public List<KundeGruppe> getCustomerGroupsByBasisGroup(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Cache fintCache = cacheManager.getCache("basisGroups");
        BasisgruppeResources resources = fintCache.get(orgId, BasisgruppeResources.class);

        return resources.getContent().stream()
                .filter(r -> r.getSkole().contains(school.getSelfLinks().stream().findAny().orElseGet(Link::new)))
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupsByTeachingGroup(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Cache fintCache = cacheManager.getCache("teachingGroups");
        UndervisningsgruppeResources resources = fintCache.get(orgId, UndervisningsgruppeResources.class);

        return resources.getContent().stream()
                .filter(r -> r.getSkole().contains(school.getSelfLinks().stream().findAny().orElseGet(Link::new)))
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupsByContactTeacherGroup(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Cache fintCache = cacheManager.getCache("contactTeacherGroups");
        KontaktlarergruppeResources kontaktlarergruppeResources = fintCache.get(orgId, KontaktlarergruppeResources.class);

        return kontaktlarergruppeResources.getContent().stream()
                .filter(r -> r.getSkole().contains(school.getSelfLinks().stream().findAny().orElseGet(Link::new)))
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    private KundeGruppe createCustomerGroup(String orgId, String navn, String beskrivelse, List<Link> elevforhold) {
        KundeGruppe kundeGruppe = new KundeGruppe();
        kundeGruppe.setNavn(navn);
        kundeGruppe.setBeskrivelse(beskrivelse);
        kundeGruppe.setKundeliste(getCustomersForGroup(orgId, elevforhold));
        return kundeGruppe;
    }

    @SuppressWarnings("unchecked")
    private List<Kunde> getCustomersForGroup(String orgId, List<Link> elevforhold) {
        Cache cache = cacheManager.getCache("customers");
        Map<Link, Kunde> customers = (Map<Link, Kunde>) cache.get(orgId).get();

        return elevforhold.stream().map(customers::get).collect(Collectors.toList());
    }
}
