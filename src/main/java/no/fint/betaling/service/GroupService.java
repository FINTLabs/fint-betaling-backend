package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.BasisgruppeResources;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResources;
import no.fint.model.resource.utdanning.elev.MedlemskapResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private RestService restService;

    @Autowired
    private KundeFactory kundeFactory;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private StudentRelationService studentRelationService;

    public List<KundeGruppe> getAllCustomerGroups(String orgId) {
        List<KundeGruppe> allGroups = new ArrayList<>();
        allGroups.addAll(getCustomerGroupListFromBasisgruppe(orgId));
        allGroups.addAll(getCustomerGroupListFromKontaktlarergruppe(orgId));
        allGroups.addAll(getCustomerGroupListFromUndervisningsgruppe(orgId));
        return allGroups;
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppe(String orgId) {
        BasisgruppeResources basisgruppeResources = restService.getResource(BasisgruppeResources.class, basisgruppeEndpoint, orgId);
        log.info(String.format("Found %s basic groups", basisgruppeResources.getContent().size()));
        return basisgruppeResources
                .getContent()
                .stream()
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getSelfLinks()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(String orgId) {
        KontaktlarergruppeResources kontaktlarergruppeResources = restService.getResource(KontaktlarergruppeResources.class, kontaktlarergruppeEndpoint, orgId);
        log.info(String.format("Found %s contact groups", kontaktlarergruppeResources.getContent().size()));
        return kontaktlarergruppeResources
                .getContent()
                .stream()
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getSelfLinks()))
                .collect(Collectors.toList());
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppe(String orgId) {
        UndervisningsgruppeResources undervisningsgruppeResources = restService.getResource(UndervisningsgruppeResources.class, undervisningsgruppeEndpoint, orgId);
        log.info(String.format("Found %s lesson groups", undervisningsgruppeResources.getContent().size()));
        return undervisningsgruppeResources
                .getContent()
                .stream()
                .map(g -> createCustomerGroup(orgId, g.getNavn(), g.getBeskrivelse(), g.getSelfLinks()))
                .collect(Collectors.toList());
    }

    private KundeGruppe createCustomerGroup(String orgId, String navn, String beskrivelse, List<Link> self) {
        KundeGruppe kundeGruppe = new KundeGruppe();
        kundeGruppe.setNavn(navn);
        kundeGruppe.setBeskrivelse(beskrivelse);
        kundeGruppe.setKundeliste(self.stream().flatMap(l -> getCustomersForGroup(orgId, l).stream()).collect(Collectors.toList()));
        return kundeGruppe;
    }

    private List<String> getCustomersForGroup(String orgId, Link group) {
        Map<Link,String> memberToCustomerMap = customerService.getCustomers(orgId,null).stream()
                .collect(Collectors.toMap(Kunde::getElev, Kunde::getKundeid));
        Map<Link,Link> studentRelationToStudentMap = studentRelationService.getStudentRelationships(orgId);
        List<MedlemskapResource> memberships = membershipService.getMemberships(orgId);

        return memberships
                .stream()
                .filter(m -> m.getGruppe().contains(group))
                .flatMap(m -> m.getMedlem().stream())
                .map(studentRelationToStudentMap::get)
                .map(memberToCustomerMap::get)
                .collect(Collectors.toList());
    }

}
