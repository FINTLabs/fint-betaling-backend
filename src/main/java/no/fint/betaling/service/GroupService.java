package no.fint.betaling.service;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    @Autowired
    RestService restService;

    @Autowired
    private KundeFactory kundeFactory;


    public List<KundeGruppe> getAllCustomerGroups(String orgId) {
        List<KundeGruppe> allCustomerGroups = new ArrayList<>();
        allCustomerGroups.addAll(getCustomerGroupListFromBasisgruppe(orgId));
        allCustomerGroups.addAll(getCustomerGroupListFromUndervisningsgruppe(orgId));
        allCustomerGroups.addAll(getCustomerGroupListFromKontaktlarergruppe(orgId));
        return allCustomerGroups;
    }

    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(String orgId) {
        KontaktlarergruppeResources kontaktlarergruppeResources = restService.getKontaktlarergruppeResources(orgId);
        List<KontaktlarergruppeResource> kontaktlarergruppeResourceList = kontaktlarergruppeResources.getContent();
        List<KundeGruppe> customerGroupList = new ArrayList<>();
        for (KontaktlarergruppeResource group : kontaktlarergruppeResourceList) {
            List<Kunde> customerList = getCustomerList(orgId, group.getMedlemskap());
            KundeGruppe customerGroup = createCustomerGroup(group.getNavn(), group.getBeskrivelse(), customerList);
            customerGroupList.add(customerGroup);
        }
        return customerGroupList;
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppe(String orgId) {
        UndervisningsgruppeResources undervisningsgruppeResources = restService.getUndervisningsgruppeResources(orgId);
        List<UndervisningsgruppeResource> undervisningsgruppeResourceList = undervisningsgruppeResources.getContent();
        List<KundeGruppe> customerGroupList = new ArrayList<>();
        for (UndervisningsgruppeResource group : undervisningsgruppeResourceList) {
            List<Kunde> customerList = getCustomerList(orgId, group.getMedlemskap());
            KundeGruppe customerGroup = createCustomerGroup(group.getNavn(), group.getBeskrivelse(), customerList);
            customerGroupList.add(customerGroup);
        }
        return customerGroupList;
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppe(String orgId) {
        BasisgruppeResources basisgruppeResources = restService.getBasisgruppeResources(orgId);
        List<BasisgruppeResource> basisgruppeResourceList = basisgruppeResources.getContent();
        List<KundeGruppe> customerGroupList = new ArrayList<>();
        for (BasisgruppeResource group : basisgruppeResourceList) {
            List<Kunde> customerList = getCustomerList(orgId, group.getMedlemskap());
            KundeGruppe customerGroup = createCustomerGroup(group.getNavn(), group.getBeskrivelse(), customerList);
            customerGroupList.add(customerGroup);
        }
        return customerGroupList;
    }

    private List<Kunde> getCustomerList(String orgId, List<Link> listMedlemskap) {
        List<Kunde> customerList = new ArrayList<>();
        for (Link linkMedlemskap : listMedlemskap) {
            MedlemskapResource resourceMeldemskap = restService.getMedlemskapResource(orgId, linkMedlemskap.getHref());
            Link studentRelation = resourceMeldemskap.getMedlem().get(0);
            ElevforholdResource elevforholdResource = restService.getElevforholdResource(orgId, studentRelation.getHref());
            List<Link> studentLinkList = elevforholdResource.getElev();
            if (studentLinkList.size() > 0) {
                Link studentLink = studentLinkList.get(0);
                ElevResource elevResource = restService.getElevResource(orgId, studentLink.getHref());
                customerList.add(kundeFactory.getKunde(orgId, elevResource));
            }
        }
        return customerList;
    }

    private KundeGruppe createCustomerGroup(String name, String description, List<Kunde> customerlist) {
        KundeGruppe kundeGruppe = new KundeGruppe();
        kundeGruppe.setNavn(name);
        kundeGruppe.setBeskrivelse(description);
        kundeGruppe.setKundeliste(customerlist);
        return kundeGruppe;
    }
}
