package no.fint.betaling.model;

import lombok.Data;

@Data
public class Lineitem {
    private String itemCode;
    private Long itemPrice;
    private Long taxrate;
    private String description;
    private String uri;
}