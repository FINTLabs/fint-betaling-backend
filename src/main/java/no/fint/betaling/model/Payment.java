package no.fint.betaling.model;


import lombok.Data;

@Data
public class Payment {
    private Fakturagrunnlag fakturagrunnlag;
    private Kunde kunde;
}
