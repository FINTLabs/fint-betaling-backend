package no.fint.betaling.factory;

import no.fint.betaling.model.*;
import no.fint.betaling.util.CloneUtil;
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
        return order
                .getCustomers()
                .stream()
                .map(customer -> createClaim(order, customer))
                .collect(Collectors.toList());
    }

    private Claim createClaim(Order order, Customer customer) {

        List<OrderItem> clonedOrderItems = order.getOrderItems().stream().map(CloneUtil::cloneObject).toList();

        Claim claim = new Claim();
        claim.setOrgId(orgId);
        claim.setCreatedDate(LocalDateTime.now());
        claim.setLastModifiedDate(LocalDateTime.now());
        claim.setOriginalAmountDue(order.sum());
        claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
        claim.setCustomerId(customer.getId());
        claim.setCustomerName(customer.getName());
        claim.setCreatedByEmployeeNumber(order.getCreatedBy().getEmployeeNumber());
        claim.setOrganisationUnit(order.getOrganisationUnit());
        claim.setPrincipalCode(order.getPrincipal().getCode());
        claim.setPrincipalUri(order.getPrincipal().getUri());
        claim.setClaimStatus(ClaimStatus.STORED);

        claim.setOrderItems(order.getOrderItems());
        clonedOrderItems.forEach(orderItem -> orderItem.setClaim(claim));

        return claim;
    }
}
