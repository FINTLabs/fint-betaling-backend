package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Order;
import no.fint.betaling.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ClaimFactory {

    @Autowired
    private ClaimRepository claimRepository;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    public List<Claim> createClaim(Order order) {
        List<Claim> claims = new ArrayList<>();

        Long orderNumber = claimRepository.getHighestOrderNumber();

        AtomicLong counter = new AtomicLong(orderNumber);

        order.getCustomers().forEach(customer -> {
            counter.getAndIncrement();
            Claim claim = new Claim();
            claim.setOrgId(orgId);
            claim.setOrderNumber(counter.toString());
            claim.setCreatedDate(LocalDate.now(ZoneId.systemDefault()));
            claim.setLastModifiedDate(LocalDate.now(ZoneId.systemDefault()));
            claim.setOriginalAmountDue(order.sum());
            claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
            claim.setCustomer(customer);
            claim.setCreatedBy(order.getCreatedBy());
            claim.setOrganisationUnit(order.getOrganisationUnit());
            claim.setPrincipal(order.getPrincipal());
            claim.setOrderItems(order.getOrderItems());
            claim.setClaimStatus(ClaimStatus.STORED);
            claims.add(claim);
        });

        return claims;
    }
}
