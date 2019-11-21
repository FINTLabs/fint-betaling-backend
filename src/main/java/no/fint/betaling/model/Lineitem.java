package no.fint.betaling.model;

import lombok.Data;

import java.net.URI;

@Data
public class Lineitem {
    private String code;
    private Double itemPrice;
    private Double taxrate;
    private String description;
    private URI uri;
}
