package no.fint.betaling.service;

import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.model.resource.utdanning.elev.ElevResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private KundeFactory kundeFactory;

    @Autowired
    private RestService restService;

    public List<Kunde> getCustomers() {
        ElevResources elevResources = restService.getElevResources();

        return elevResources.getContent().stream()
                .map(elev -> kundeFactory.getKunde(elev))
                .collect(Collectors.toList());
    }

    public List<Kunde> getCustomers(String filter) {
        ElevResources elevResources = restService.getElevResources();

        List<Kunde> allCustomers = elevResources.getContent().stream()
                .map(elev -> kundeFactory.getKunde(elev))
                .collect(Collectors.toList());

        return allCustomers.stream().filter(customer ->
                customer.getNavn().getEtternavn().toLowerCase()
                        .contains(filter.toLowerCase()))
                        .collect(Collectors.toList());
    }
}
