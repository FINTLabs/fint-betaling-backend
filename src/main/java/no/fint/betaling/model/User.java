package no.fint.betaling.model;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String name;
    private Organization organization;
    private List<Organization> schools;
}