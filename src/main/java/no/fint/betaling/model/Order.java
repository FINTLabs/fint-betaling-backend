package no.fint.betaling.model;

import lombok.Data;

import java.util.List;

@Data
public class Order {
    private List<OrderItem> orderItems;
    private List<Customer> customers;
    private String requestedNumberOfDaysToPaymentDeadline;
    private Organisation organisationUnit;
    private Principal principal;
    private User createdBy;

    public Long sum() {
        return orderItems.stream().mapToLong(OrderItem::sum).sum();
    }
}
