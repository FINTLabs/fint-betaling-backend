package no.fint.betaling.service;

import no.fint.model.administrasjon.okonomi.Varelinje;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderLineService {

    @Value("${fint.betaling.endpoints.orderLine}")
    private String orderLineUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<VarelinjeResource> getOrderLines(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        return restTemplate.exchange(
                orderLineUrl + "/varelinje",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VarelinjeResources.class
        ).getBody().getContent();
    }

    public boolean setOrderLine(String orgId, Varelinje orderLine) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        HttpEntity<Varelinje> httpEntity = new HttpEntity<>(orderLine,headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                orderLineUrl + "/varelinje",
                HttpMethod.POST,
                httpEntity,
                Varelinje.class
        );
        return responseEntity.getStatusCode().is2xxSuccessful();
    }
}
