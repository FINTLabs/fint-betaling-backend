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

    public List<Kunde> getCustomers(String orgId) {
        return getCustomers(orgId, "");
    }

    public List<Kunde> getCustomers(String orgId, String filter) {
        if (filter == null) {
            filter = "";
        }
        ElevResources elevResources = restService.getResource(ElevResources.class, RestService.ELEV_ENDPOINT, orgId);

        List<Kunde> allCustomers = elevResources.getContent().stream()
                .map(elev -> kundeFactory.getKunde(orgId, elev))
                .collect(Collectors.toList());

        String finalFilter = filter;
        return allCustomers.stream().filter(customer ->
                customer.getNavn().getEtternavn().toLowerCase()
                        .contains(finalFilter.toLowerCase()))
                .collect(Collectors.toList());
    }
}
