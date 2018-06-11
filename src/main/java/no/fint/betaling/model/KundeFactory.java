package no.fint.betaling.model;

import no.fint.betaling.service.PersonService;
import no.fint.model.felles.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;

@Service
public class KundeFactory {

    @Autowired
    private PersonService personService;

    public Kunde getKunde(Resource elev) {
        String personUrl = elev.getLinks().get(0).getHref(); // TODO finn linken basert på "person"

        Person person = personService.getPerson(personUrl);

        Kunde customer = new Kunde();
        customer.setKundenummer(person.getFodselsnummer().getIdentifikatorverdi());
        customer.setNavn(person.getNavn());
        customer.setKontaktinformasjon(person.getKontaktinformasjon());
        customer.setPostadresse(person.getPostadresse());

        return customer;
    }
}
