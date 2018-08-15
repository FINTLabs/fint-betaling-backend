package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;

@Data
public class Kunde {
    private String kundenummer;
    private Personnavn navn;
    private String fulltNavn;
    private Link person;
    private Link elev;
}
