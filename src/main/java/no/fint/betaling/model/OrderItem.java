package no.fint.betaling.model;

import lombok.Data;

@Data
public class OrderItem {
    private String description;
    private Long itemQuantity;
    private Long itemPrice;
    private Lineitem lineitem;

    public Long sum() {
        if (itemPrice == null) {
            return itemQuantity * lineitem.getItemPrice();
        }
        return itemQuantity * itemPrice;
    }
}
