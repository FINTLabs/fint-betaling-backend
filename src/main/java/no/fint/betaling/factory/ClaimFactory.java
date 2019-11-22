package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Order;
import no.fint.betaling.repository.OrderNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ClaimFactory {

    @Autowired
    private OrderNumberRepository orderNumberRepository;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    public List<Claim> createClaim(Order order) {
        List<Claim> claims = new ArrayList<>();

        Long orderNumber = orderNumberRepository.getHighestOrderNumber();

        AtomicLong counter = new AtomicLong(orderNumber);

        order.getCustomers().forEach(customer -> {
            counter.getAndIncrement();
            Claim claim = new Claim();
            claim.setOrgId(orgId);
            claim.setOrderNumber(counter.toString());
            claim.setCustomer(customer);
            claim.setPrincipal(order.getPrincipal());
            claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
            claim.setOriginalAmountDue(order.sum());
            claim.setOrderItems(order.getOrderItems());
            claim.setClaimStatus(ClaimStatus.STORED);
            claims.add(claim);
        });

        return claims;
    }
}
