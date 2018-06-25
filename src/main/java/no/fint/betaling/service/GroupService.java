package no.fint.betaling.service;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import no.fint.model.utdanning.basisklasser.Gruppe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private RestService restService;

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
        KontaktlarergruppeResources kontaktlarergruppeResources = restService.getResource(KontaktlarergruppeResources.class, RestService.KONTAKTLARERGRUPPE_ENDPOINT, orgId);
        return getCustomerGroup(orgId, kontaktlarergruppeResources.getContent());
    }

    public List<KundeGruppe> getCustomerGroupListFromUndervisningsgruppe(String orgId) {
        UndervisningsgruppeResources undervisningsgruppeResources = restService.getResource(UndervisningsgruppeResources.class, RestService.UNDERVISNINGSGRUPPE_ENDPOINT, orgId);
        return getCustomerGroup(orgId, undervisningsgruppeResources.getContent());
    }

    public List<KundeGruppe> getCustomerGroupListFromBasisgruppe(String orgId) {
        BasisgruppeResources basisgruppeResources = restService.getResource(BasisgruppeResources.class, RestService.BASISGRUPPE_ENDPOINT, orgId);
        return getCustomerGroup(orgId, basisgruppeResources.getContent());
    }

    private List<KundeGruppe> getCustomerGroup(String orgId, List<? extends Gruppe> content) {
        return content.stream().map(group -> {
            List<Link> medlemskap = getMedlemskapLinks(group);
            List<Kunde> customerList = getCustomerList(orgId, medlemskap);
            return createCustomerGroup(group.getNavn(), group.getBeskrivelse(), customerList);
        }).collect(Collectors.toList());
    }

    private List<Link> getMedlemskapLinks(Gruppe gruppe) {
        return ((FintLinks) gruppe).getLinks().get("medlemskap");
    }

    private List<Kunde> getCustomerList(String orgId, List<Link> listMedlemskap) {
        List<Kunde> customerList = new ArrayList<>();
        for (Link linkMedlemskap : listMedlemskap) {
            MedlemskapResource resourceMeldemskap = restService.getResource(MedlemskapResource.class, linkMedlemskap.getHref(), orgId);
            Link studentRelation = resourceMeldemskap.getMedlem().get(0);
            ElevforholdResource elevforholdResource = restService.getResource(ElevforholdResource.class, studentRelation.getHref(), orgId);
            List<Link> studentLinkList = elevforholdResource.getElev();
            if (studentLinkList.size() > 0) {
                Link studentLink = studentLinkList.get(0);
                ElevResource elevResource = restService.getResource(ElevResource.class, studentLink.getHref(), orgId);
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
