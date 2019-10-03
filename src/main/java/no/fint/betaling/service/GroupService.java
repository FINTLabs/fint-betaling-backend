package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.exception.ResourceNotFoundException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.util.ResourceCache;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class GroupService {

    @Value("${fint.betaling.endpoints.skole}")
    private String skoleEndpoint;

    @Value("${fint.betaling.endpoints.basisgruppe}")
    private String basisgruppeEndpoint;

    @Value("${fint.betaling.endpoints.undervisningsgruppe}")
    private String undervisningsgruppeEndpoint;

    @Value("${fint.betaling.endpoints.kontaktlarergruppe}")
    private String kontaktlarergruppeEndpoint;

    @Value("${fint.betaling.endpoints.student}")
    private String elevEndpoint;

    @Value("${fint.betaling.endpoints.student-relation}")
    private String elevforholdEndpoint;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private KundeFactory kundeFactory;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private StudentRelationService studentRelationService;

    private ResourceCache<SkoleResource> skoleResourceResourceCache;
    private ResourceCache<BasisgruppeResource> basisgruppeResourceResourceCache;
    private ResourceCache<KontaktlarergruppeResource> kontaktlarergruppeResourceResourceCache;
    private ResourceCache<UndervisningsgruppeResource> undervisningsgruppeResourceResourceCache;
    private ResourceCache<ElevResource> elevResourceResourceCache;
    private ResourceCache<ElevforholdResource> elevforholdResourceResourceCache;

    @PostConstruct
    public void init() {
        skoleResourceResourceCache = new ResourceCache<>(cacheService, skoleEndpoint, SkoleResources.class);
        basisgruppeResourceResourceCache = new ResourceCache<>(cacheService, basisgruppeEndpoint, BasisgruppeResources.class);
        kontaktlarergruppeResourceResourceCache = new ResourceCache<>(cacheService, kontaktlarergruppeEndpoint, KontaktlarergruppeResources.class);
        undervisningsgruppeResourceResourceCache = new ResourceCache<>(cacheService, undervisningsgruppeEndpoint, UndervisningsgruppeResources.class);
        elevResourceResourceCache = new ResourceCache<>(cacheService, elevEndpoint, ElevResources.class);
        elevforholdResourceResourceCache = new ResourceCache<>(cacheService, elevforholdEndpoint, ElevforholdResources.class);
    }

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:360000}")
    public void updateCaches() {
        skoleResourceResourceCache.updateCaches();
        basisgruppeResourceResourceCache.updateCaches();
        kontaktlarergruppeResourceResourceCache.updateCaches();
        undervisningsgruppeResourceResourceCache.updateCaches();
        elevResourceResourceCache.updateCaches();
        elevforholdResourceResourceCache.updateCaches();
    }

    public List<KundeGruppe> getAllCustomerGroups(String orgId) {
        List<KundeGruppe> allGroups = new ArrayList<>();
        allGroups.addAll(getCustomerGroupListFromBasisgruppe(orgId));
        allGroups.addAll(getCustomerGroupListFromKontaktlarergruppe(orgId));
        allGroups.addAll(getCustomerGroupListFromUndervisningsgruppe(orgId));
        return allGroups;
    }

    private SkoleResource getSchool(String orgId, String schoolOrgId) {
        List<SkoleResource> skoleResources = skoleResourceResourceCache.getResources(orgId);
        return skoleResources
                .stream()
                .filter(s -> s.getOrganisasjonsnummer().getIdentifikatorverdi().equals(schoolOrgId))
                .findAny()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public KundeGruppe getCustomerGroupFromSchool(String orgId, String schoolOrgId) {
        SkoleResource skoleResource = getSchool(orgId, schoolOrgId);
        return createCustomerGroup(orgId, skoleResource.getNavn(), skoleResource.getNavn(), skoleResource.getElevforhold());
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppeAtSchool(String orgId, String schoolOrgId) {
        SkoleResource skoleResource = getSchool(orgId, schoolOrgId);
        List<BasisgruppeResource> basisgruppeResources = basisgruppeResourceResourceCache.getResources(orgId);
        return basisgruppeResources
                .stream()
                .filter(g -> g.getSkole().equals(skoleResource.getSelfLinks()))
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromKontaktLarergruppeAtSchool(String orgId, String schoolOrgId) {
        SkoleResource skoleResource = getSchool(orgId, schoolOrgId);
        List<KontaktlarergruppeResource> kontaktlarergruppeResources = kontaktlarergruppeResourceResourceCache.getResources(orgId);
        return kontaktlarergruppeResources
                .stream()
                .filter(g -> g.getSkole().equals(skoleResource.getSelfLinks()))
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppeAtSchool(String orgId, String schoolOrgId) {
        SkoleResource skoleResource = getSchool(orgId, schoolOrgId);
        List<UndervisningsgruppeResource> undervisningsgruppeResources = undervisningsgruppeResourceResourceCache.getResources(orgId);
        return undervisningsgruppeResources
                .stream()
                .filter(g -> g.getSkole().equals(skoleResource.getSelfLinks()))
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppe(String orgId) {
        List<BasisgruppeResource> basisgruppeResources = basisgruppeResourceResourceCache.getResources(orgId);
        log.info(String.format("Found %s basic groups", basisgruppeResources.size()));
        return basisgruppeResources
                .stream()
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(String orgId) {
        List<KontaktlarergruppeResource> kontaktlarergruppeResources = kontaktlarergruppeResourceResourceCache.getResources(orgId);
        log.info(String.format("Found %s contact groups", kontaktlarergruppeResources.size()));
        return kontaktlarergruppeResources
                .stream()
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppe(String orgId) {
        List<UndervisningsgruppeResource> undervisningsgruppeResources = undervisningsgruppeResourceResourceCache.getResources(orgId);
        log.info(String.format("Found %s lesson groups", undervisningsgruppeResources.size()));
        return undervisningsgruppeResources
                .stream()
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

    private List<Kunde> getCustomersForGroup(String orgId, List<Link> elevforhold) {
        List<Link> elever = elevResourceResourceCache.getResources(orgId)
                .stream()
                .filter(e -> e.getElevforhold().stream().anyMatch(elevforhold::contains))
                .flatMap(e -> e.getPerson().stream())
                .collect(Collectors.toList());

        List<Kunde> kunder = customerService.getPersonResourceResourceCache().getResources(orgId)
                .stream()
                .filter(p -> p.getSelfLinks().stream().anyMatch(elever::contains))
                .map(kundeFactory::getKunde)
                .collect(Collectors.toList());

        return kunder;



        /*
        Map<Link, Kunde> memberToCustomerMap = customerService.getCustomers(orgId, null).stream()
                .collect(Collectors.toMap(Kunde::getElev, Function.identity(), (a, b) -> a));
        Map<Link, Link> studentRelationToStudentMap = studentRelationService.getStudentRelationships(orgId);

        return elevforhold
                .stream()
                .map(studentRelationToStudentMap::get)
                .map(memberToCustomerMap::get);

         */
    }
}
