package no.fint.betaling.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "claim")
public class Claim {
    private String orgId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "claim_sequence_generator")
    @SequenceGenerator(name = "claim_sequence_generator", sequenceName = "claim_seq", allocationSize = 1, initialValue = 10000)
    private long orderNumber;

    @Column(name = "invoiceNumbers")
    private String invoiceNumbersCommaSeperated;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastModifiedDate;

    @Temporal(TemporalType.DATE)
    private LocalDate invoiceDate;

    @Temporal(TemporalType.DATE)
    private LocalDate paymentDueDate;

    private Long amountDue;

    private Long originalAmountDue;

    private String requestedNumberOfDaysToPaymentDeadline;

    private String customerId;

    private String customerName;

    private String createdByEmployeeNumber;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "organisationNumber")
    private Organisation organisationUnit;

    private String principalCode;

    private String principalUri;

    private String invoiceUri;

    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "claim")
    private List<OrderItem> orderItems;

    private ClaimStatus claimStatus;

    private String statusMessage;

    private Long timestamp;

    public Set<String> getInvoiceNumbers() {
        if (invoiceNumbersCommaSeperated == null){
            return Set.of();
        }

        return Set.of(invoiceNumbersCommaSeperated.split(","));
    }

    public void setInvoiceNumbers(Set<String> invoiceNumbers) {
        if (invoiceNumbers !=null) {
            invoiceNumbersCommaSeperated = String.join(",", invoiceNumbers);
        }
    }
}
