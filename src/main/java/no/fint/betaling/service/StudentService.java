package no.fint.betaling.service;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.model.utdanning.elev.Elev;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KundeFactory kundeFactory;

    @Value("${fint.betaling.endpoints.students:https://play-with-fint.felleskomponent.no/utdanning/elev/elev}")
    private String studentEndpoint;

    public List<Kunde> getCustomers() {
        ResponseEntity<Resources<Resource<Elev>>> response = restTemplate.exchange(
                studentEndpoint,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resources<Resource<Elev>>>() {
                }
        );

        List<Kunde> customerList = new ArrayList<>();

        response.getBody().getContent().forEach(elev -> {
            Kunde customer = kundeFactory.getKunde(elev);
            customerList.add(customer);
        });

        return customerList;
    }


}
