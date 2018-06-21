package no.fint.betaling.model;


import lombok.Data;
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;

@Data
public class Payment {
    private Fakturagrunnlag fakturagrunnlag;
    private Kunde kunde;
}
