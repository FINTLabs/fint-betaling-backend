
package no.fint.betaling.service;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private KundeFactory kundeFactory;

    @Value("https://play-with-fint.felleskomponent.no/utdanning/elev/")
    private String groupEndpoint;

    public List<KundeGruppe> getCustomerGroups() {
        List<KundeGruppe> customerGroupListBasisgruppe = getCustomerGroupListFromBasisgruppe();


        List<KundeGruppe> customerGroupListAll = customerGroupListBasisgruppe.stream().collect(Collectors.toList());

        return customerGroupListAll;
    }

    private List<KundeGruppe> getCustomerGroupListFromBasisgruppe() {
        ResponseEntity<BasisgruppeResources> responseBasisgruppeResources = restTemplate.exchange(
                groupEndpoint + "basisgruppe",
                HttpMethod.GET,
                null,
                BasisgruppeResources.class
        );

        List<BasisgruppeResource> basisgruppeResourceList = responseBasisgruppeResources.getBody().getContent();

        List<Kunde> customerList = new ArrayList<>();
        List<KundeGruppe> customerGroupList = new ArrayList<>();

        for (BasisgruppeResource basisgruppeResource : basisgruppeResourceList) {

            for (Link linkMedlemskap : basisgruppeResource.getMedlemskap()) {
                ResponseEntity<MedlemskapResource> responseMedlemskap = restTemplate.exchange(
                        linkMedlemskap.getHref(),
                        HttpMethod.GET,
                        null,
                        MedlemskapResource.class
                );

                Link studentRelation = responseMedlemskap.getBody().getMedlem().get(0);

                ResponseEntity<ElevforholdResource> responseElevforhold = restTemplate.exchange(
                        studentRelation.getHref(),
                        HttpMethod.GET,
                        null,
                        ElevforholdResource.class
                );

                List<Link> studentLinkList = responseElevforhold.getBody().getElev();

                if (studentLinkList.size() > 0) {
                    Link studentLink = studentLinkList.get(0);
                    ResponseEntity<ElevResource> responseElevResource = restTemplate.exchange(
                            studentLink.getHref(),
                            HttpMethod.GET,
                            null,
                            ElevResource.class
                    );

                    ElevResource student = responseElevResource.getBody();
                    customerList.add(kundeFactory.getKunde(student));
                }
            }
            KundeGruppe customerGroup = new KundeGruppe();
            customerGroup.setNavn(basisgruppeResource.getNavn());
            customerGroup.setBeskrivelse(basisgruppeResource.getBeskrivelse());
            customerGroup.setKundeliste(customerList);
            customerGroupList.add(customerGroup);
        }

        return customerGroupList;
    }
}
