package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
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

@Slf4j
@Service
public class GroupService {

    @Autowired
    private RestService restService;

    @Autowired
    private KundeFactory kundeFactory;

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

    public List<KundeGruppe> getAllCustomerGroups(String orgId) {
        List<KundeGruppe> allCustomerGroups = new ArrayList<>();
        allCustomerGroups.addAll(getCustomerGroupListFromBasisgruppe(orgId));
        allCustomerGroups.addAll(getCustomerGroupListFromUndervisningsgruppe(orgId));
        allCustomerGroups.addAll(getCustomerGroupListFromKontaktlarergruppe(orgId));
        return allCustomerGroups;
    }

    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(String orgId) {
        KontaktlarergruppeResources kontaktlarergruppeResources = restService.getResource(KontaktlarergruppeResources.class, kontaktlarergruppeEndpoint, orgId);
        return getCustomerGroup(orgId, kontaktlarergruppeResources.getContent());
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppe(String orgId) {
        UndervisningsgruppeResources undervisningsgruppeResources = restService.getResource(UndervisningsgruppeResources.class, undervisningsgruppeEndpoint, orgId);
        return getCustomerGroup(orgId, undervisningsgruppeResources.getContent());
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppe(String orgId) {
        BasisgruppeResources basisgruppeResources = restService.getResource(BasisgruppeResources.class, basisgruppeEndpoint, orgId);
        return getCustomerGroup(orgId, basisgruppeResources.getContent());
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
        /*
        persons.forEach(person -> {
            students.forEach(student -> {
                if (student.getPerson().contains(person.getSelfLinks().get(0))) {
                    studentRelations.forEach(relation -> {
                        if (relation.getLinks().get("self").contains(student.getElevforhold().get(0))) {
                            memberships.forEach(membership -> {
                                membershipCustomer.put(membership.getLinks().get("self").get(0), kundeFactory.getKunde(person));
                            });
                        }
                    });
                }
            });
        });

        Map<Link, Kunde> membershipCustomer = persons.stream().reduce(HashMap<Link, Kunde>)
        */

        Map<Link, Kunde> membershipCustomer = new HashMap<>();

        for (PersonResource person : persons) {
            for (ElevResource student : students) {
                if (student.getPerson().contains(person.getLinks().get("self").get(0))) {
                    for (ElevforholdResource relation : studentRelations) {
                        if (relation.getLinks().get("self").contains(student.getElevforhold().get(0))) {
                            for (MedlemskapResource membership : memberships) {
                                if (membership.getLinks().get("self").contains(relation.getMedlemskap().get(0))) {
                                    membershipCustomer.put(membership.getLinks().get("self").get(0), kundeFactory.getKunde(person));
                                }
                            }
                        }
                    }
                }
            }
        }


            Map<Gruppe, List<Kunde>> groupMemberMap = new HashMap<>();
            for (Gruppe group : groups) {
                FintLinks groupLink = (FintLinks) group;
                List<Kunde> members = new ArrayList<>();
                for (Link membership : groupLink.getLinks().get("medlemskap")) {
                    members.add(membershipCustomer.get(membership));
                }
                groupMemberMap.put(group, members);
            }
            log.info("All customers added to groups");
            return groupMemberMap;
        }

        private KundeGruppe createCustomerGroup (String name, String description, List < Kunde > customerlist){
            KundeGruppe kundeGruppe = new KundeGruppe();
            kundeGruppe.setNavn(name);
            kundeGruppe.setBeskrivelse(description);
            kundeGruppe.setKundeliste(customerlist);
            return kundeGruppe;
        }
    }
