package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.util.ResourceCache;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResources;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResource;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResources;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class GroupService {

    @Value(("${fint.betaling.endpoints.basisgruppe}"))
    private String basisgruppeEndpoint;

    @Value("${fint.betaling.endpoints.undervisningsgruppe}")
    private String undervisningsgruppeEndpoint;

    @Value("${fint.betaling.endpoints.kontaktlarergruppe}")
    private String kontaktlarergruppeEndpoint;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private StudentRelationService studentRelationService;

    private ResourceCache<BasisgruppeResource> basisgruppeResourceResourceCache;
    private ResourceCache<KontaktlarergruppeResource> kontaktlarergruppeResourceResourceCache;
    private ResourceCache<UndervisningsgruppeResource> undervisningsgruppeResourceResourceCache;

    @PostConstruct
    public void init() {
        basisgruppeResourceResourceCache = new ResourceCache<>(cacheService, basisgruppeEndpoint, BasisgruppeResources.class);
        kontaktlarergruppeResourceResourceCache = new ResourceCache<>(cacheService, kontaktlarergruppeEndpoint, KontaktlarergruppeResources.class);
        undervisningsgruppeResourceResourceCache = new ResourceCache<>(cacheService, undervisningsgruppeEndpoint, UndervisningsgruppeResources.class);
    }

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:360000}")
    public void updateCaches() {
        basisgruppeResourceResourceCache.updateCaches();
        kontaktlarergruppeResourceResourceCache.updateCaches();
        undervisningsgruppeResourceResourceCache.updateCaches();
    }


    public List<KundeGruppe> getAllCustomerGroups(String orgId) {
        List<KundeGruppe> allGroups = new ArrayList<>();
        allGroups.addAll(getCustomerGroupListFromBasisgruppe(orgId));
        allGroups.addAll(getCustomerGroupListFromKontaktlarergruppe(orgId));
        allGroups.addAll(getCustomerGroupListFromUndervisningsgruppe(orgId));
        return allGroups;
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
        kundeGruppe.setKundeliste(getCustomersForGroup(orgId, elevforhold).collect(Collectors.toList()));
        return kundeGruppe;
    }

    private Stream<Kunde> getCustomersForGroup(String orgId, List<Link> elevforhold) {
        Map<Link, Kunde> memberToCustomerMap = customerService.getCustomers(orgId, null).stream()
                .collect(Collectors.toMap(Kunde::getElev, Function.identity(), (a, b) -> a));
        Map<Link, Link> studentRelationToStudentMap = studentRelationService.getStudentRelationships(orgId);

        return elevforhold
                .stream()
                .map(studentRelationToStudentMap::get)
                .map(memberToCustomerMap::get);
    }

}
