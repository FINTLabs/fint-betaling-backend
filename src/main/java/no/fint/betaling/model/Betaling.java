package no.fint.betaling.model;

import lombok.Data;

@Data
public class Betaling {
    private String ordrenummer;
    private String fakturanummer;
    private String restBelop;
    private Kunde kunde;
    private Fakturagrunnlag fakturagrunnlag;
    private boolean sendt = false;
}
