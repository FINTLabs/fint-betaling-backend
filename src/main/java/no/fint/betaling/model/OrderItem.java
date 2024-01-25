package no.fint.betaling.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orderitem")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderitem_sequence_generator")
    @SequenceGenerator(name = "orderitem_sequence_generator", sequenceName = "orderitem_seq", allocationSize = 1)
    private long id;

    private String description;

    private Long itemQuantity;

    private Long itemPrice;

    private String itemCode;

    private Long originalItemPrice;

    private Long taxRate;

    private String originalDescription;

    private String itemUri;

    public Long sum() {
        return itemPrice == null ? itemQuantity * originalItemPrice : itemQuantity * itemPrice;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "claim_id")
    private Claim claim;
}
