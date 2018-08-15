package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.resource.Link;

@Data
public class Kunde {
    private String kundenummer;
    private String navn;
    private Link person;
    private Link elev;
}
