package no.fint.betaling.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "lineitem")
public class Lineitem {
    @Id
    private String itemCode;
    private Long itemPrice;
    private Long taxrate;
    private String description;
    private String uri;
}