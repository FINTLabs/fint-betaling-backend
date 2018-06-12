package no.fint.betaling.service;

import no.fint.betaling.model.InvalidResponseException;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PersonService {

    @Autowired
    private RestTemplate restTemplate;

    public PersonResource getPerson(String personUrl) {
        try {
            return restTemplate.getForObject(personUrl, PersonResource.class);
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get person resource url: %s", personUrl), e);
        }
    }
}
