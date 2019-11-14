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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ClaimFactory {

    @Autowired
    private OrderNumberRepository orderNumberRepository;

    @Value("${fint.betaling.org-id}")
    private String orgId;

    public List<Claim> createClaim(Order order) {
        List<Claim> claims = new ArrayList<>();

        Long orderNumber = orderNumberRepository.getHighestOrderNumber();

        AtomicLong counter = new AtomicLong(1);

        order.getCustomers().forEach(customer -> {
            Claim claim = new Claim();
            claim.setOrgId(orgId);
            claim.setOrderNumber(String.valueOf(orderNumber + counter.longValue()));
            claim.setCustomer(customer);
            claim.setPrincipalUri(order.getPrincipalUri());
            claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
            claim.setOriginalAmountDue(order.sum());
            claim.setOrderLines(order.getOrderLines());
            claim.setClaimStatus(ClaimStatus.STORED);
            counter.getAndIncrement();
            claims.add(claim);
        });
        return claims;
    }


    /*
    public List<Claim> createClaim(Order order) {
        List<Claim> claims = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger(0);

        long test = orderNumberRepository.getNewOrderNumber();

        order.getCustomers().(customer -> {
            Claim claim = new Claim();
            claim.setOrgId(orgId);
            claim.setOrderNumber("");
            claim.setCustomer(customer);
            claim.setPrincipalUri(order.getPrincipalUri());
            claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
            claim.setOriginalAmountDue(order.sum());
            claim.setOrderLines(order.getOrderLines());
            claim.setClaimStatus(ClaimStatus.STORED);
            claims.add(claim);
        });
        return claims;
    }

     */
}
