package no.fint.betaling.service;

import no.fint.betaling.model.InvalidResponseException;
import no.fint.model.felles.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PersonService {

    @Autowired
    private RestTemplate restTemplate;

    public Person getPerson(String personUrl){
        try {
            ResponseEntity<Resource<Person>> responsePerson = restTemplate.exchange(
                    personUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Resource<Person>>() {
                    }
            );
            return responsePerson.getBody().getContent();
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get person resource url: %s", personUrl), e);
        }
    }
}
