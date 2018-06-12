package no.fint.betaling.service;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.model.KundeGruppe;
import no.fint.model.utdanning.elev.Basisgruppe;
import no.fint.model.utdanning.elev.Elev;
import no.fint.model.utdanning.elev.Elevforhold;
import no.fint.model.utdanning.elev.Medlemskap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
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
        ResponseEntity<Resources<Resource<Basisgruppe>>> response = restTemplate.exchange(
                groupEndpoint + "basisgruppe",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resources<Resource<Basisgruppe>>>() {
                }
        );

        Collection<Resource<Basisgruppe>> customerGroupCollection = response.getBody().getContent();
        List<KundeGruppe> customerGroupList = new ArrayList<>();
        for (Resource<Basisgruppe> basisgruppeResource : customerGroupCollection) {
            List<Kunde> customerlist = new ArrayList<>();
            List<Link> linkList = basisgruppeResource.getLinks();
            List<Link> filteredLinks = linkList.stream().filter(link -> link.getRel().equals("medlemskap")).collect(Collectors.toList());

            for (Link link : filteredLinks) {
                ResponseEntity<Resources<Resource<Medlemskap>>> membershipResponse = restTemplate.exchange(
                        link.getHref(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Resources<Resource<Medlemskap>>>() {
                        }
                );
                String studentMembershipUrl = membershipResponse.getBody().getLink("medlem").getHref();

                ResponseEntity<Resources<Resource<Elevforhold>>> studentMembershipResponse = restTemplate.exchange(
                        studentMembershipUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Resources<Resource<Elevforhold>>>() {
                        }
                );
                String studentUrl = studentMembershipResponse.getBody().getLink("elev").getHref();

                ResponseEntity<Resource<Elev>> studentResponse = restTemplate.exchange(
                        studentUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Resource<Elev>>() {
                        }
                );
                Kunde customer = kundeFactory.getKunde(studentResponse.getBody());
                customerlist.add(customer);
            }

            Basisgruppe basisgruppe = basisgruppeResource.getContent();
            KundeGruppe customerGroup = new KundeGruppe();
            customerGroup.setNavn(basisgruppe.getNavn());
            customerGroup.setBeskrivelse(basisgruppe.getBeskrivelse());
            customerGroup.setKundeliste(customerlist);
            customerGroupList.add(customerGroup);
        }


        return customerGroupList;
    }
}
