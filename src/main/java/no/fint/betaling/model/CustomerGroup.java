package no.fint.betaling.model;

import lombok.Data;

import java.util.List;

@Data
public class CustomerGroup {
    private String name;
    private String description;
    private List<Customer> customers;
}
