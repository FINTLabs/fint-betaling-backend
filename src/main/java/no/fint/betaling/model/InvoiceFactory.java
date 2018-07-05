package no.fint.betaling.model;

import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceFactory {

    public FakturagrunnlagResource getInvoice(Betaling payment) {
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
        Long netto = payment.getVarelinjer().stream().mapToLong(VarelinjeResource::getPris).sum();

        FakturagrunnlagResource invoice = new FakturagrunnlagResource();
        invoice.setFakturalinjer(paymentLines);
        invoice.setFakturadato(new Date());
        invoice.setNetto(netto);

        return invoice;
    }
}
