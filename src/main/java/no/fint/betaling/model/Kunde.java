package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;

@Data
public class Kunde {
    private String kundenummer;
    private Personnavn navn;
    private String fulltNavn;
    private Kontaktinformasjon kontaktinformasjon;
    private AdresseResource postadresse;
    private Link person;
    private Link elev;
}
