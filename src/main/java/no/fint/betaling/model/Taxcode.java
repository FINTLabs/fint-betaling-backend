package no.fint.betaling.model;

import lombok.Data;

@Data
public class Taxcode {
    private String code;
    private Long rate;
    private String description;
    private String uri;
}