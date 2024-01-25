package no.fint.betaling.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "creditnote")
public class CreditNote {

    private String orderNumber;

    // todo: Is id uniq for each creditnote or for all creditnotes?
    @Id
    private String id;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime date;

    private Long amount;

    private String comment;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "claim_id")
    private Claim claim;
}
