package no.fint.betaling.model;


import lombok.*;
import no.fint.model.FintMainObject;
import no.fint.model.felles.kompleksedatatyper.Identifikator;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Fakturagrunnlag implements FintMainObject {

    public enum Relasjonsnavn {
        MOTTAKER,
        OPPDRAGSGIVER
    }

    private long avgifter;
    private Date fakturadato;
    private List<Fakturalinje> fakturalinjer;
    private Identifikator fakturanummer;
    private Date forfallsdato;
    private Date leveringsdato;
    private long netto;
    private long total;

    @NonNull
    private Identifikator systemId;

}
