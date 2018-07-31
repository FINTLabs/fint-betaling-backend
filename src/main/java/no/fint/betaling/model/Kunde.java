package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.resource.Link;

@Data
public class Kunde {
    private String kundeid;
    private String navn;
    private Link person;
    private Link elev;
}
