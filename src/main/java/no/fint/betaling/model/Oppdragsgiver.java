package no.fint.betaling.model;

import lombok.*;
import no.fint.model.felles.kompleksedatatyper.Identifikator;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Oppdragsgiver {
    public enum Relasjonsnavn {
        ORGANISASJONSELEMENT,
        FAKRUTAGRUNNLAG,
        VARELINJE
    }
    private String navn;
    @NonNull
    private Identifikator systemId;
}
