package no.fint.betaling.model;

import lombok.*;
import no.fint.model.FintMainObject;
import no.fint.model.felles.kompleksedatatyper.Identifikator;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Fakturalinje implements FintMainObject {
    public enum Relasjonsnavn {
        VARELINJE
    }

    private long antall;
    private String fritekst;
    private long pris;

    @NonNull
    private Identifikator systemId;


}
