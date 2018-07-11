package no.fint.betaling.model;


import lombok.Data;
import lombok.NonNull;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;

import java.util.List;

@Data
public class Payment {
    private List<OrderLine> orderLines;
    private List<Kunde> customers;
    private OppdragsgiverResource employer;
    private String timeFrameDueDate;
}
