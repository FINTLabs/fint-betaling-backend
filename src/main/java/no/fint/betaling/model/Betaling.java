package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;

@Data
public class Betaling {
    private String ordrenummer;
    private String fakturanummer;
    private String restBelop;
    private Kunde kunde;
    private Fakturagrunnlag fakturagrunnlag;
    private boolean sentToExternalSystem = false;
}
