package no.fint.betaling.model;

import no.fint.model.resource.felles.PersonResource;
import org.springframework.stereotype.Service;

@Service
public class KundeFactory {

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

