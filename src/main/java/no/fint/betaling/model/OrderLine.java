package no.fint.betaling.model;

import lombok.Data;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource;

@Data
public class OrderLine {
    private VarelinjeResource orderLine;
    private String ammount;
    private String description;
}
