package no.fint.betaling.service;

import no.fint.model.administrasjon.okonomi.Varelinje;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderLineService {

    private String ORDERLINEURL = "https://beta1.felleskomponent.no/administrasjon/okonomi";

    @Autowired
    private RestTemplate restTemplate;

    public List<Varelinje> getOrderLines(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        ResponseEntity responseEntity = restTemplate.exchange(
                ORDERLINEURL + "/varelinje",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VarelinjeResources.class
        );
        return new ArrayList<>();
    }

    public boolean setOrderLine(String orgId, Varelinje orderLine) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        HttpEntity<Varelinje> httpEntity = new HttpEntity<>(orderLine,headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                ORDERLINEURL + "/varelinje",
                HttpMethod.POST,
                httpEntity,
                Varelinje.class
        );
        return responseEntity.getStatusCode().is2xxSuccessful();
    }
}
