package no.fint.betaling.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "claim")
public class Claim {
    private String orgId;

    @Id
    private String orderNumber;

    @Column(name = "invoiceNumbers")
    private String invoiceNumbersCommaSeperated;

    private LocalDate invoiceDate;

    private LocalDate paymentDueDate;

    private LocalDate createdDate;

    private LocalDate lastModifiedDate;

    @OneToMany(cascade = CascadeType.ALL)
    private List<CreditNote> creditNotes;

    private Long amountDue;

    private Long originalAmountDue;

    private String requestedNumberOfDaysToPaymentDeadline;

    @OneToOne(cascade = CascadeType.ALL)
    private Customer customer;

    private String createdByEmployeeNumber;

    @OneToOne(cascade = CascadeType.ALL)
    private Organisation organisationUnit;

    private String principalCode;

    private String principalUri;

    private String invoiceUri;

    @OneToMany(cascade = CascadeType.ALL)
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
        invoiceNumbersCommaSeperated = String.join(",", invoiceNumbers);
    }
}
