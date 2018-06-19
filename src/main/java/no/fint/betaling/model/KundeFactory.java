package no.fint.betaling.model;

import no.fint.betaling.service.RestService;
import no.fint.model.felles.Person;
import no.fint.model.resource.FintLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KundeFactory {

    @Autowired
    private RestService restService;

    public Kunde getKunde(String orgId, FintLinks links) {
        String personUrl = links.getLinks().get("person").get(0).getHref();
        Person person = restService.getPerson(orgId, personUrl);

        Kunde customer = new Kunde();
        customer.setKundenummer(person.getFodselsnummer().getIdentifikatorverdi());
        customer.setNavn(person.getNavn());
        customer.setKontaktinformasjon(person.getKontaktinformasjon());
        customer.setPostadresse(person.getPostadresse());

        return customer;
    }
}
