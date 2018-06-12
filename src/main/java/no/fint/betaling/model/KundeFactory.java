package no.fint.betaling.model;

import no.fint.betaling.service.PersonService;
import no.fint.model.felles.Person;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KundeFactory {

    @Autowired
    private PersonService personService;

    public Kunde getKunde(FintLinks links) {
        String personUrl = links.getLinks().get("person").get(0).getHref();
        PersonResource person = personService.getPerson(personUrl);
        Kunde customer = new Kunde();
        customer.setKundenummer(person.getFodselsnummer().getIdentifikatorverdi());
        customer.setNavn(person.getNavn());
        customer.setKontaktinformasjon(person.getKontaktinformasjon());
        customer.setPostadresse(person.getPostadresse());

        return customer;
    }
}
