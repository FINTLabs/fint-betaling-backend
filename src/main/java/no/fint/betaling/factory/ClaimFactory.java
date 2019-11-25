package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        claim.setCreatedDate(LocalDate.now());
        claim.setLastModifiedDate(LocalDate.now());
        claim.setOriginalAmountDue(order.sum());
        claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
        claim.setCustomer(customer);
        claim.setCreatedBy(order.getCreatedBy());
        claim.setOrganisationUnit(order.getOrganisationUnit());
        claim.setPrincipal(order.getPrincipal());
        claim.setOrderItems(order.getOrderItems());
        claim.setClaimStatus(ClaimStatus.STORED);
        return claim;
    }
}
