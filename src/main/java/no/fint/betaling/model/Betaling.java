package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.administrasjon.okonomi.Fakturagrunnlag;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;

import java.net.URI;
import java.util.List;

@Data
public class Betaling {
    private String ordrenummer;
    private String fakturanummer;
    private String restBelop;
    private long timeFrameDueDate;
    private Kunde kunde;
    private OppdragsgiverResource oppdragsgiver;
    private URI location;
    private Fakturagrunnlag fakturagrunnlag;
    private List<VarelinjeResource> varelinjer;
    private boolean sentTilEksterntSystem = false;
    private enum status {
        ACCEPTED, PENDING, FAILED, NOTSENDT
    }
}
