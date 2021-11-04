package no.fint.betaling.model;

import lombok.Data;

import java.util.Set;

@Data
public class Principal {
    private String code;
    private String description;
    private Set<Lineitem> lineitems;
    private String uri;
    private Organisation organisation;
}