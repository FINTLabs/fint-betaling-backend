package no.fint.betaling.model.dto;

import lombok.Getter;
import lombok.Setter;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ClaimDto {

    private String orgId;
    private Long orderNumber;
    private LocalDateTime createdDate;
    private LocalDate invoiceDate;
    private LocalDate paymentDueDate;
    private Long amountDue;
    private Long originalAmountDue;
    private String requestedNumberOfDaysToPaymentDeadline;
    private String customerName;
    private String createdByEmployee;
    private Organisation organisationUnit;
    private String principalCode;
    private String invoiceUri;
    private List<OrderItemDto> orderItems;
    private ClaimStatus claimStatus;
    private String statusMessage;
    private Long timestamp;
    private Set<String> invoiceNumbers;

    public ClaimDto(Claim claim) {
        this.orgId = claim.getOrgId();
        this.orderNumber = claim.getOrderNumber();
        this.createdDate = claim.getCreatedDate();
        this.invoiceDate = claim.getInvoiceDate();
        this.paymentDueDate = claim.getPaymentDueDate();
        this.amountDue = claim.getAmountDue();
        this.originalAmountDue = claim.getOriginalAmountDue();
        this.requestedNumberOfDaysToPaymentDeadline =
                claim.getRequestedNumberOfDaysToPaymentDeadline();
        this.customerName = claim.getCustomerName();
        this.createdByEmployee = "";
        this.organisationUnit = claim.getOrganisationUnit();
        this.principalCode = claim.getPrincipalCode();
        this.invoiceUri = claim.getInvoiceUri();
        this.claimStatus = claim.getClaimStatus();
        this.statusMessage = claim.getStatusMessage();
        this.timestamp = claim.getTimestamp();
        this.invoiceNumbers = claim.getInvoiceNumbers();

        this.orderItems = claim.getOrderItems()
                .stream()
                .map(OrderItemDto::new)
                .toList();
    }

    public ClaimDto(Claim claim, String createdByEmployee) {
        this.orgId = claim.getOrgId();
        this.orderNumber = claim.getOrderNumber();
        this.createdDate = claim.getCreatedDate();
        this.invoiceDate = claim.getInvoiceDate();
        this.paymentDueDate = claim.getPaymentDueDate();
        this.amountDue = claim.getAmountDue();
        this.originalAmountDue = claim.getOriginalAmountDue();
        this.requestedNumberOfDaysToPaymentDeadline =
                claim.getRequestedNumberOfDaysToPaymentDeadline();
        this.customerName = claim.getCustomerName();
        this.createdByEmployee = createdByEmployee;
        this.organisationUnit = claim.getOrganisationUnit();
        this.principalCode = claim.getPrincipalCode();
        this.invoiceUri = claim.getInvoiceUri();
        this.claimStatus = claim.getClaimStatus();
        this.statusMessage = claim.getStatusMessage();
        this.timestamp = claim.getTimestamp();
        this.invoiceNumbers = claim.getInvoiceNumbers();

        this.orderItems = claim.getOrderItems()
                .stream()
                .map(OrderItemDto::new)
                .toList();
    }

}
