package no.fint.betaling.model;


import lombok.Data;
import no.fint.model.administrasjon.okonomi.Varelinje;

import java.util.List;

@Data
public class Payment {
    private List<Varelinje> orderLines;
    private List<Kunde> customers;
}
