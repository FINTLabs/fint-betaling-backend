package no.fint.betaling.model;


import lombok.Data;
import no.fint.model.resource.administrasjon.okonomi.MvakodeResource;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;

import java.util.List;

@Data
public class Payment {
    private List<VarelinjeResource> orderLines;
    private List<Kunde> customers;
    private MvakodeResource mvaCode;
    private OppdragsgiverResource employer;
}
