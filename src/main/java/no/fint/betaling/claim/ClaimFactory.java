package no.fint.betaling.claim;

import no.fint.betaling.model.*;
import no.fint.betaling.common.util.CloneUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimFactory {

    @Value("${fint.betaling.org-id}")
    private String orgId;

    public List<Claim> createClaims(Order order) {
        LocalDateTime now = LocalDateTime.now();

        return order
                .getCustomers()
                .stream()
                .map(customer -> createClaim(order, customer, now))
                .collect(Collectors.toList());
    }

    private Claim createClaim(Order order, Customer customer, LocalDateTime creationDateTime) {

        List<OrderItem> clonedOrderItems = order.getOrderItems().stream().map(CloneUtil::cloneObject).toList();

        Claim claim = new Claim();
        claim.setOrgId(orgId);
        claim.setCreatedDate(creationDateTime);
        claim.setLastModifiedDate(creationDateTime);
        claim.setOriginalAmountDue(order.sum());
        claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
        claim.setCustomerId(customer.getId());
        claim.setCustomerName(customer.getName());
        claim.setCreatedByEmployeeNumber(order.getCreatedBy().getEmployeeNumber());
        claim.setOrganisationUnit(order.getOrganisationUnit());
        claim.setPrincipalCode(order.getPrincipal().getCode());
        claim.setPrincipalUri(order.getPrincipal().getUri());
        claim.setClaimStatus(ClaimStatus.STORED);

        claim.setOrderItems(clonedOrderItems);
        clonedOrderItems.forEach(orderItem -> orderItem.setClaim(claim));

        // Moving from NO-SQL to SQL, so we change OrderItem id from internal counter to sequence
        clonedOrderItems.forEach(orderItem -> orderItem.setId(0L));

        return claim;
    }
}
