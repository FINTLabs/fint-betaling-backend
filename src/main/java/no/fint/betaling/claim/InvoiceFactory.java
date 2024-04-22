package no.fint.betaling.claim;

import no.fint.betaling.model.Claim;
import no.fint.betaling.organisation.PersonService;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import no.fint.model.resource.okonomi.faktura.FakturalinjeResource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceFactory {

    private final PersonService personService;

    public InvoiceFactory(PersonService personService) {
        this.personService = personService;
    }

    public FakturagrunnlagResource createInvoice(Claim claim) {
        List<FakturalinjeResource> invoiceLines = claim.getOrderItems().stream().map(orderItem -> {
            FakturalinjeResource invoiceLine = new FakturalinjeResource();
            invoiceLine.setPris(orderItem.getItemPrice() != null ? orderItem.getItemPrice() : orderItem.getOriginalItemPrice());
            invoiceLine.setAntall(orderItem.getItemQuantity() / 1.0f);
            invoiceLine.setFritekst(Collections.singletonList(orderItem.getDescription()));
            invoiceLine.addVare(Link.with(orderItem.getItemUri()));
            return invoiceLine;
        }).toList();


        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        invoice.setFakturalinjer(invoiceLines);
        invoice.setLeveringsdato(startOfDayFrom(claim.getCreatedDate()));
        invoice.setNettobelop(claim.getOriginalAmountDue());
        invoice.setMottaker(personService.getFakturamottakerByPersonId(claim.getCustomerId()));
        invoice.addFakturautsteder(Link.with(claim.getPrincipalUri()));

        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(String.valueOf(claim.getOrderNumber()));
        invoice.setOrdrenummer(identifikator);

        return invoice;
    }

    private static Date startOfDayFrom(LocalDateTime localDateTime) {
        return Date.from(localDateTime.with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant());
    }
}
