package no.fint.betaling.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreditNote {

    private String orderNumber;

    // todo: Is id uniq for each creditnote or for all creditnotes?
    @Id
    private String id;

    private LocalDateTime date;

    private Long amount;

    private String comment;

    private Claim claim;
}
