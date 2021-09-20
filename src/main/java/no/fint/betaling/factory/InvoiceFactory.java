package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.betaling.service.PersonService;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import no.fint.model.resource.okonomi.faktura.FakturalinjeResource;
import no.fint.model.resource.okonomi.faktura.FakturamottakerResource;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceFactory {

    private final PersonService personService;

    public InvoiceFactory(PersonService personService) {
        this.personService = personService;
    }

    public FakturagrunnlagResource createInvoice(Claim claim) {
        List<FakturalinjeResource> invoiceLines = claim.getOrderItems().stream().map(orderItem -> {
            FakturalinjeResource invoiceLine = new FakturalinjeResource();
            if (orderItem.getItemPrice() != null) {
                invoiceLine.setPris(orderItem.getItemPrice());
            } else {
                invoiceLine.setPris(orderItem.getLineitem().getItemPrice());
            }
            invoiceLine.setAntall(orderItem.getItemQuantity() / 1.0f);
            invoiceLine.setFritekst(Collections.singletonList(orderItem.getDescription()));
            invoiceLine.addVare(Link.with(orderItem.getLineitem().getUri()));
            return invoiceLine;
        }).collect(Collectors.toList());


        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        invoice.setFakturalinjer(invoiceLines);
        invoice.setLeveringsdato(Date.from(claim.getCreatedDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        invoice.setNettobelop(claim.getOriginalAmountDue());
        invoice.setMottaker(personService.getFakturamottakerByPersonId(claim.getCustomer().getId()));
        invoice.addFakturautsteder(Link.with(claim.getPrincipal().getUri()));

        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(claim.getOrderNumber());
        invoice.setOrdrenummer(identifikator);

        return invoice;
    }
}
