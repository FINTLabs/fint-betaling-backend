package no.fint.betaling.model;

import no.fint.betaling.service.RestService;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KundeFactory {

    @Autowired
    private RestService restService;

    public Kunde getKunde(String orgId, FintLinks links) {
        String personUrl = links.getLinks().get("person").get(0).getHref();
        PersonResource person = restService.getResource(PersonResource.class, personUrl, orgId);
        if (person != null) {
            Kunde customer = new Kunde();
            customer.setKundenummer(person.getFodselsnummer().getIdentifikatorverdi());
            customer.setNavn(person.getNavn());
            customer.setLinkTilPerson(personUrl);
            customer.setKontaktinformasjon(person.getKontaktinformasjon());
            customer.setPostadresse(person.getPostadresse());
            return customer;
        }
        return null;
    }

    public Kunde getKunde(PersonResource person) {
        Kunde customer = new Kunde();
        customer.setKundenummer(person.getFodselsnummer().getIdentifikatorverdi());
        customer.setNavn(person.getNavn());
        customer.setLinkTilPerson(person.getLinks().get("self").get(0).getHref());
        customer.setKontaktinformasjon(person.getKontaktinformasjon());
        customer.setPostadresse(person.getPostadresse());
        return customer;
    }
}

