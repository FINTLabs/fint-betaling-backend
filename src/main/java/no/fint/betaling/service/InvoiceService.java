package no.fint.betaling.service;

import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    @Value("${fint.betaling.endpoints.invoice}")
    private String invoiceEndpoint;

    @Autowired
    private RestService restService;

    public List<FakturagrunnlagResource> getInvoice(String orgId) {
        return restService.getResource(FakturagrunnlagResources.class, invoiceEndpoint, orgId).getContent();
    }

    public ResponseEntity setInvoice(String orgId, FakturagrunnlagResource invoice) {
        return restService.setResource(FakturagrunnlagResource.class, invoiceEndpoint, invoice, orgId);
    }
}