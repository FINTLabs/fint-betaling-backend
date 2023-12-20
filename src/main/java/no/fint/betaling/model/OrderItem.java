package no.fint.betaling.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orderitem")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String description;

    private Long itemQuantity;

    private Long itemPrice;

    @OneToOne()
    private Lineitem lineitem;

    public Long sum() {
        if (itemPrice == null) {
            return itemQuantity * lineitem.getItemPrice();
        }
        return itemQuantity * itemPrice;
    }
}
