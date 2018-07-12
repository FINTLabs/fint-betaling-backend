package no.fint.betaling.model;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceFactory {

    public static FakturagrunnlagResource getInvoice(Betaling payment) {
        List<FakturalinjeResource> paymentLines = payment.getVarelinjer().stream().map(orderLine -> {
            FakturalinjeResource paymentLine = new FakturalinjeResource();
            paymentLine.setPris(orderLine.getOrderLine().getPris());
            List<String> description = new ArrayList<>();
            description.add(orderLine.getOrderLine().getNavn());
            description.add(orderLine.getDescription());
            paymentLine.setAntall(orderLine.getAmount());
            paymentLine.setFritekst(description);
            paymentLine.addVarelinje(orderLine.getOrderLine().getLinks().get("self").get(0));
            return paymentLine;
        }).collect(Collectors.toList());

        long sum = 0L;
        for (OrderLine orderLine : payment.getVarelinjer()) {
            VarelinjeResource line = orderLine.getOrderLine();
            long pris = line.getPris();
            long amount = orderLine.getAmount();
            sum += pris * amount;
        }
        Long netto = sum * 100;
        Long total = sum * 125;

        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        invoice.setFakturalinjer(paymentLines);
        invoice.setFakturadato(new Date());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, Integer.parseInt(payment.getTimeFrameDueDate()));

        invoice.setForfallsdato(calendar.getTime());
        invoice.setLeveringsdato(new Date());
        invoice.setNetto(netto);
        invoice.setTotal(total);
        invoice.setAvgifter(total-netto);
        invoice.addMottaker(new Link(payment.getKunde().getLinkTilPerson()));
        invoice.addOppdragsgiver(new Link(payment.getOppdragsgiver().getLinks().get("self").get(0).getHref()));

        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(payment.getOrdrenummer());
        invoice.setOrdrenummer(identifikator);

        return invoice;
    }
}
