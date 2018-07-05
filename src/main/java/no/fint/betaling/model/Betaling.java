package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;
import no.fint.model.administrasjon.okonomi.Varelinje;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;

import java.util.List;

@Data
public class Betaling {
    private String ordrenummer;
    private String fakturanummer;
    private String restBelop;
    private Kunde kunde;
    private Fakturagrunnlag fakturagrunnlag;
    private List<VarelinjeResource> varelinjer;
    private boolean sentTilEksterntSystem = false;
}
