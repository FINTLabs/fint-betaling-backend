package no.fint.betaling.model.dto;

import lombok.Getter;
import lombok.Setter;
import no.fint.betaling.model.OrderItem;

@Getter
@Setter
public class OrderItemDto {

    private String description;

    private Long itemQuantity;

    private Long itemPrice;

    private String itemCode;

    private Long originalItemPrice;

    private Long taxRate;

    private String originalDescription;

    public OrderItemDto(OrderItem orderItem) {
        this.description = orderItem.getDescription();
        this.itemQuantity = orderItem.getItemQuantity();
        this.itemPrice = orderItem.getItemPrice();
        this.itemCode = orderItem.getItemCode();
        this.originalItemPrice = orderItem.getOriginalItemPrice();
        this.taxRate = orderItem.getTaxRate();
        this.originalDescription = orderItem.getOriginalDescription();

    }

}
