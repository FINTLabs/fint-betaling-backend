package no.fint.betaling.model;

import lombok.Data;

import java.util.List;

@Data
public class KundeGruppe {
    private String navn;
    private String beskrivelse;
    private List<String> kundeliste;
}
