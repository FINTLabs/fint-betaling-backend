package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.utdanning.basisklasser.Gruppe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Value("${fint.betaling.endpoints.membership}")
    private String membershipEndpoint;

    @Value("${fint.betaling.endpoints.student-relation}")
    private String studentRelationEndpoint;

    @Value("${fint.betaling.endpoints.student}")
    private String studentEndpoint;

    @Value("${fint.betaling.endpoints.person}")
    private String personEndpoint;

    @Autowired
    private RestService restService;

    @Autowired
    private KundeFactory kundeFactory;

    public List<KundeGruppe> getAllCustomerGroups(String orgId) {
        List<Gruppe> allGroups = new ArrayList<>();
        allGroups.addAll(restService.getResource(BasisgruppeResources.class, basisgruppeEndpoint, orgId)
                .getContent().stream().map(basisgruppeResource -> (Gruppe) basisgruppeResource).collect(Collectors.toList()));
        allGroups.addAll(restService.getResource(KontaktlarergruppeResources.class, kontaktlarergruppeEndpoint, orgId)
                .getContent().stream().map(kontaktlarergruppeResource -> (Gruppe) kontaktlarergruppeResource).collect(Collectors.toList()));
        allGroups.addAll(restService.getResource(UndervisningsgruppeResources.class, undervisningsgruppeEndpoint, orgId)
                .getContent().stream().map(undervisningsgruppeResource -> (Gruppe) undervisningsgruppeResource).collect(Collectors.toList()));
        log.info(String.format("Found %s groups", allGroups.size()));
        return getCustomerGroup(orgId, allGroups);
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppe(String orgId) {
        BasisgruppeResources basisgruppeResources = restService.getResource(BasisgruppeResources.class, basisgruppeEndpoint, orgId);
        log.info(String.format("Found %s basic groups", basisgruppeResources.getContent().size()));
        return getCustomerGroup(orgId, basisgruppeResources.getContent());
    }

    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(String orgId) {
        KontaktlarergruppeResources kontaktlarergruppeResources = restService.getResource(KontaktlarergruppeResources.class, kontaktlarergruppeEndpoint, orgId);
        log.info(String.format("Found %s contact groups", kontaktlarergruppeResources.getContent().size()));
        return getCustomerGroup(orgId, kontaktlarergruppeResources.getContent());
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppe(String orgId) {
        UndervisningsgruppeResources undervisningsgruppeResources = restService.getResource(UndervisningsgruppeResources.class, undervisningsgruppeEndpoint, orgId);
        log.info(String.format("Found %s lesson groups", undervisningsgruppeResources.getContent().size()));
        return getCustomerGroup(orgId, undervisningsgruppeResources.getContent());
    }

    private List<KundeGruppe> getCustomerGroup(String orgId, List<? extends Gruppe> groups) {
        Map<Gruppe, List<Kunde>> groupMap = getGroupMap(orgId, groups);
        List<KundeGruppe> customerGroupList = new ArrayList<>();
        groupMap.forEach((Gruppe group, List<Kunde> customerList) -> {
            customerGroupList.add(createCustomerGroup(group.getNavn(), group.getBeskrivelse(), customerList));
        });
        return customerGroupList;
    }

    private Map<Gruppe, List<Kunde>> getGroupMap(String orgId, List<? extends Gruppe> groups) {
        List<MedlemskapResource> memberships = restService.getResource(MedlemskapResources.class, membershipEndpoint, orgId).getContent();
        List<ElevforholdResource> studentRelations = restService.getResource(ElevforholdResources.class, studentRelationEndpoint, orgId).getContent();
        List<ElevResource> students = restService.getResource(ElevResources.class, studentEndpoint, orgId).getContent();
        List<PersonResource> persons = restService.getResource(PersonResources.class, personEndpoint, orgId).getContent();
        log.info(String.format("Found: %s memberships, %s student-relations, %s students, %s people", memberships.size(), studentRelations.size(), students.size(), persons.size()));

        Map<Gruppe, List<Kunde>> groupCustomers = new HashMap<>();
        for (Gruppe group : groups) {
            List<MedlemskapResource> groupMemberships = getMemberships((FintLinks) group, memberships);
            List<ElevforholdResource> groupRelations = filterList(studentRelations, groupMemberships, "self");
            List<ElevResource> groupStudents = filterList(students, groupRelations, "elevforhold");
            List<PersonResource> groupPersons = filterList(persons, groupStudents, "self");
            List<Kunde> customerList = groupPersons.stream().map(kundeFactory::getKunde).collect(Collectors.toList());
            groupCustomers.put(group, customerList);
        }
        log.info("Groupmaps created");
        return groupCustomers;
    }

    private List<MedlemskapResource> getMemberships(FintLinks group, List<MedlemskapResource> memberships) {
        return memberships.stream().filter(membership -> {
            return group.getLinks().get("medlemskap").contains(membership.getLinks().get("self").get(0));
        }).collect(Collectors.toList());
    }

    private <T extends FintLinks, R extends FintLinks> List<T> filterList(List<T> list1, List<R> list2, String query) {
        return list1.stream().filter(element -> {
            return list2.stream().anyMatch(element2 -> element2.getLinks().containsValue(element.getLinks().get(query)));
        }).collect(Collectors.toList());
    }

    private KundeGruppe createCustomerGroup(String name, String description, List<Kunde> customerlist) {
        KundeGruppe kundeGruppe = new KundeGruppe();
        kundeGruppe.setNavn(name);
        kundeGruppe.setBeskrivelse(description);
        kundeGruppe.setKundeliste(customerlist);
        return kundeGruppe;
    }
}
