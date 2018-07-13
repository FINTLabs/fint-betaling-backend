package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.InvalidResponseException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentService {

    @Autowired
    private RestService restService;

    @Autowired
    private KundeFactory kundeFactory;

    @Value("${fint.betaling.endpoints.person}")
    private String personEndpoint;

    public List<Kunde> getCustomers(String orgId, String filter) {
        if (filter == null) {
            filter = "";
        }
        PersonResources personResources = restService.getResource(PersonResources.class, personEndpoint, orgId);
        log.info(String.format("Found %s people", personResources.getContent().size()));
        List<Kunde> allCustomers = new ArrayList<>();
        for (PersonResource person : personResources.getContent()) {
            try {
                Kunde customer = kundeFactory.getKunde(person);
                if (customer != null) {
                    allCustomers.add(customer);
                }
            } catch (InvalidResponseException e) {
                log.info(e.toString());
            }
        }

        String finalFilter = filter;
        return allCustomers.stream().filter(customer ->
                customer.getNavn().getEtternavn().toLowerCase()
                        .contains(finalFilter.toLowerCase()))
                .collect(Collectors.toList());
    }
}
