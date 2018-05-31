package no.fint.betaling.model;

import no.fint.model.felles.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KundeFactory {

    @Autowired
    RestTemplate restTemplate;

    public Kunde getKunde(Resource elev) {
        String personUrl = elev.getLinks().get(0).getHref();

        ResponseEntity<Resource<Person>> responsePerson = restTemplate.exchange(
                personUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resource<Person>>() {
                }
        );

        Person person = responsePerson.getBody().getContent();

        Kunde customer = new Kunde();
        customer.setKundenummer(person.getFodselsnummer().getIdentifikatorverdi());
        customer.setNavn(person.getNavn());
        customer.setKontaktinformasjon(person.getKontaktinformasjon());
        customer.setPostadresse(person.getPostadresse());

        return customer;
    }
}
