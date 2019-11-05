package no.fint.betaling.factory;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.Order;
import no.fint.betaling.repository.OrderNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimFactory {

    @Autowired
    private OrderNumberRepository orderNumberRepository;

    public List<Claim> createClaim(Order order, String orgId) {
        return order.getCustomers().stream().map(customer -> {
            Claim claim = new Claim();
            claim.setOrderNumber(orderNumberRepository.getOrderNumber(orgId));
            claim.setCustomer(customer);
            claim.setPrincipalUri(order.getPrincipalUri());
            claim.setRequestedNumberOfDaysToPaymentDeadline(order.getRequestedNumberOfDaysToPaymentDeadline());
            claim.setOriginalAmountDue(order.sum());
            claim.setClaimStatus(ClaimStatus.STORED);
            return claim;
        }).collect(Collectors.toList());
    }
}
