package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource;

import java.util.*;
import java.util.stream.Collectors;

public enum InvoiceFactory {
    ;

    public static FakturagrunnlagResource createInvoice(Claim claim) {
        List<FakturalinjeResource> paymentLines = claim.getOrderLines().stream().map(orderLine -> {
            FakturalinjeResource paymentLine = new FakturalinjeResource();
            paymentLine.setPris(orderLine.getItemPrice());
            paymentLine.setAntall(orderLine.getNumberOfItems() / 1.0f);
            paymentLine.setFritekst(Collections.singletonList(orderLine.getDescription()));
            paymentLine.addVarelinje(Link.with(orderLine.getItemUri().toString()));
            return paymentLine;
        }).collect(Collectors.toList());


        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        invoice.setFakturalinjer(paymentLines);
        invoice.setFakturadato(new Date());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, Integer.parseInt(claim.getRequestedNumberOfDaysToPaymentDeadline()));

        invoice.setForfallsdato(calendar.getTime());
        invoice.setLeveringsdato(new Date());
        invoice.setNetto(claim.getOriginalAmountDue());
        invoice.addMottaker(Link.with(claim.getCustomer().getPerson().toString()));
        invoice.addOppdragsgiver(Link.with(claim.getPrincipalUri().toString()));

        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(claim.getOrderNumber());
        invoice.setOrdrenummer(identifikator);

        return invoice;
    }
}
