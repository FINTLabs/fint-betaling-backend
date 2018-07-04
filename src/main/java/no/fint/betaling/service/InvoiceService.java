package no.fint.betaling.service;

import no.fint.betaling.model.Betaling;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResources;
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    public List<FakturagrunnlagResource> getFakturagrunnlag(String orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        return restTemplate.exchange(
                invoiceEndpoint,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FakturagrunnlagResources.class
        ).getBody().getContent();
    }

    public boolean setFakturagrunnlag(String orgId, FakturagrunnlagResource payment) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-org-id", orgId);
        HttpEntity<FakturagrunnlagResource> entity = new HttpEntity<>(payment, headers);
        ResponseEntity response = restTemplate.exchange(
                invoiceEndpoint,
                HttpMethod.POST,
                entity,
                FakturagrunnlagResource.class
        );
        return response.getStatusCode().is2xxSuccessful();
    }

    public FakturagrunnlagResource createFakturagrunnlagResource(Betaling payment) {
        List<FakturalinjeResource> paymentLines = payment.getVarelinjer().stream().map(orderLine -> {
            FakturalinjeResource paymentLine = new FakturalinjeResource();
            paymentLine.setPris(orderLine.getPris());
            List<String> description = new ArrayList<>();
            description.add(orderLine.getNavn());
            description.add(orderLine.getEnhet());
            description.add(orderLine.getKode());
            paymentLine.setFritekst(description);
            return paymentLine;
        }).collect(Collectors.toList());

        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        invoice.setFakturalinjer(paymentLines);

        return invoice;
    }
}
