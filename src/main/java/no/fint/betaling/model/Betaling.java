package no.fint.betaling.model;

import lombok.Data;
import no.fint.betaling.model.vocab.BetalingStatus;
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;

import java.util.List;

@Data
public class Betaling {
    private Long ordrenummer;
    private Long fakturanummer;
    private String restBelop;
    private String timeFrameDueDate;
    private Kunde kunde;
    private OppdragsgiverResource oppdragsgiver;
    private String location;
    private FakturagrunnlagResource fakturagrunnlag;
    private List<OrderLine> varelinjer;
    private BetalingStatus status;
    private String error = null;
    private boolean sentTilEksterntSystem = false;
}
