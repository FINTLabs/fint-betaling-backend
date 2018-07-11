package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.InvalidResponseException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevResources;
import no.fint.model.utdanning.elev.Elev;
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
    private KundeFactory kundeFactory;

    @Autowired
    private RestService restService;

    @Value("${fint.betaling.endpoints.student}")
    private String elevEndpoint;

    @Value("${fint.betaling.endpoints.person}")
    private String personEndpoint;

    public List<Kunde> getCustomers(String orgId) {
        return getCustomers(orgId, "");
    }

    public List<Kunde> getCustomersFromElev(String orgId, String filter) {
        if (filter == null) {
            filter = "";
        }
        ElevResources elevResources = restService.getResource(ElevResources.class, elevEndpoint, orgId);
        log.info(String.format("Found %s students", elevResources.getContent().size()));
        List<Kunde> allCustomers = new ArrayList<>();
        int i = 0;
        for (ElevResource student : elevResources.getContent()){
            try {
                Kunde customer = kundeFactory.getKunde(orgId, student);
                if (customer != null){
                    allCustomers.add(customer);
                    log.info(String.format("Added customer nr: %s", i++));
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

    public List<Kunde> getCustomers(String orgId, String filter) {
        if (filter == null) {
            filter = "";
        }
        PersonResources personResources = restService.getResource(PersonResources.class, personEndpoint, orgId);
        log.info(String.format("Found %s people", personResources.getContent().size()));
        List<Kunde> allCustomers = new ArrayList<>();
        int i = 0;
        for (PersonResource person : personResources.getContent()){
            try {
                Kunde customer = kundeFactory.getKunde(person);
                if (customer != null){
                    allCustomers.add(customer);
                    log.info(String.format("Added customer nr: %s", i++));
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
