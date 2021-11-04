package no.fint.betaling.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreditNote {
    private String id;
    private LocalDate date;
    private Long amount;
    private String comment;
}
