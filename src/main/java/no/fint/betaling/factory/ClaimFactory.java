package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.Order;
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
        Claim claim = new Claim();
        claim.setOrgId(orgId);
        claim.setCreatedDate(LocalDateTime.now());
        claim.setLastModifiedDate(LocalDateTime.now());
        claim.setOriginalAmountDue(order.sum());
        claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
        claim.setCustomer(customer);
        claim.setCreatedByEmployeeNumber(order.getCreatedBy().getEmployeeNumber());
        claim.setOrganisationUnit(order.getOrganisationUnit());
        claim.setPrincipalCode(order.getPrincipal().getCode());
        claim.setPrincipalUri(order.getPrincipal().getUri());
        claim.setOrderItems(order.getOrderItems());
        claim.setClaimStatus(ClaimStatus.STORED);
        return claim;
    }
}
