package no.fint.betaling.model;

import lombok.Data;

import java.net.URI;
import java.util.Set;

@Data
public class Principal {
    private String code;
    private String description;
    private Set<String> lineitems;
    private URI uri;
}
