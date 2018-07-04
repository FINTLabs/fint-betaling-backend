package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;
import no.fint.model.administrasjon.okonomi.Varelinje;

import java.util.List;

@Data
public class Betaling {
    private String ordrenummer;
    private String fakturanummer;
    private String restBelop;
    private Kunde kunde;
    private Fakturagrunnlag fakturagrunnlag;
    private List<Varelinje> varelinjer;
    private boolean sentTilEksterntSystem = false;
}
