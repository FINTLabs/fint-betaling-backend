package no.fint.betaling.service;

import no.fint.betaling.model.InvalidResponseException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.model.resource.utdanning.elev.ElevResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KundeFactory kundeFactory;

    @Value("${fint.betaling.endpoints.students:https://play-with-fint.felleskomponent.no/utdanning/elev/elev}")
    private String studentEndpoint;

    public List<Kunde> getCustomers() {
        ElevResources elevResources = getElevResources();
        return elevResources.getContent().stream()
                .map(elev -> kundeFactory.getKunde(elev))
                .collect(Collectors.toList());
    }

    private ElevResources getElevResources() {
        try {
            return restTemplate.getForObject(studentEndpoint, ElevResources.class);
        } catch (RestClientException e) {
            throw new InvalidResponseException(String.format("Unable to get elev resource url: %s", studentEndpoint), e);
        }
    }
}
