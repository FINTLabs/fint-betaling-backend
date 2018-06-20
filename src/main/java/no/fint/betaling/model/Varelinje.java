package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Periode;

@Data
public class Varelinje {
    private String enhet;
    //private Kontostreng kontering;
    private long pris;
    private Periode gyldighetsperiode;
    private String kode;
    private String navn;
    private boolean passiv;
    private Identifikator systemId;

    private Mvakode mvakode;
    private Oppdragsgiver oppdragsgiver;
}
