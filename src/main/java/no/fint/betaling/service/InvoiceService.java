package no.fint.betaling.service;

import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;
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
public class InvoiceService {

    private String FAKTURAGRUNNLAG_URL = "https://beta1.felleskomponent.no/administrasjon/okonomi/fakturagrunnlag";

    @Autowired
    private RestTemplate restTemplate;

    public List<Fakturagrunnlag> getFakturagrunnlag(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        /*
        return restTemplate.exchange(
            FAKTURAGRUNNLAG_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Fakturagrunnlag.class
        )
        */
        return new ArrayList<>();
    }

    public boolean setFakturagrunnlag(String orgId, Fakturagrunnlag payment) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        HttpEntity<Fakturagrunnlag> entity = new HttpEntity<>(payment, headers);
        ResponseEntity response = restTemplate.exchange(
                FAKTURAGRUNNLAG_URL,
                HttpMethod.POST,
                entity,
                Fakturagrunnlag.class
        );
        return response.getStatusCode().is2xxSuccessful();
    }
}
