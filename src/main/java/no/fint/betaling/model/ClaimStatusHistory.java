package no.fint.betaling.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "claim_status_history_sequence_generator")
    @SequenceGenerator(name = "claim_status_history_sequence_generator", sequenceName = "history_seq", allocationSize = 1, initialValue = 10000)
    private Long id;

    private Long orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(100)")
    private ClaimStatus status;

    private String statusMessage;

    private LocalDateTime modifiedAt;

    public ClaimStatusHistory(Claim claim, ClaimStatus status) {
        this.orderNumber = claim.getOrderNumber();
        this.status = status;
        this.statusMessage = claim.getStatusMessage();
        this.modifiedAt = LocalDateTime.now();
    }

    public ClaimStatusHistory() {

    }
}
