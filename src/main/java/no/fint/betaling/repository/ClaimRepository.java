package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.OrderItem;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
public class ClaimRepository {

    private final ClaimJpaRepository claimJpaRepository;

    private final OrganisationJpaRepository organisationJpaRepository;

    private final OrderItemJpaRepository orderItemJpaRepository;

    public ClaimRepository(ClaimJpaRepository claimJpaRepository, OrganisationJpaRepository organisationJpaRepository, OrderItemJpaRepository orderItemJpaRepository) {
        this.claimJpaRepository = claimJpaRepository;
        this.organisationJpaRepository = organisationJpaRepository;
        this.orderItemJpaRepository = orderItemJpaRepository;
    }

    public Claim get(long orderNumber) {
        return claimJpaRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + orderNumber));
    }

    public List<Claim> get(ClaimStatus... statuses) {
        return claimJpaRepository.findByClaimStatusIn(statuses);
    }

    public synchronized Claim storeClaim(Claim claim) {

        if (claim.getOrganisationUnit() != null) {
            organisationJpaRepository.save(claim.getOrganisationUnit());
        }

        claim.setTimestamp(System.currentTimeMillis());
        claim = claimJpaRepository.save(claim);

        if (claim.getOrderItems() != null) {
            for (OrderItem orderItem : claim.getOrderItems()) {
                orderItem.setClaim(claim);
            }

            orderItemJpaRepository.saveAll(claim.getOrderItems());
        }

        return claim;
    }

    public List<Claim> getByCustomerName(String name) {
        return claimJpaRepository.findByCustomerName(name);
    }

    public int countByStatus(ClaimStatus... statuses) {
        return claimJpaRepository.countByStatus(statuses);
    }

    public int countByStatusAndDays(long days, ClaimStatus[] statuses) {
        return claimJpaRepository.countByStatusAndDays(days, statuses);
    }

    public List<Claim> getByDateAndSchoolAndStatus(LocalDateTime date, String organisationNumber, ClaimStatus[] statuses) {
        String orgNumber = StringUtils.hasText(organisationNumber) ? organisationNumber : null;
        List<ClaimStatus> statusList = (statuses != null && statuses.length > 0) ? Arrays.asList(statuses) : null;
        return claimJpaRepository.getByDateAndSchoolAndStatus(date, orgNumber, statusList);
    }

    public void save(Claim claim) {
        claim.setTimestamp(System.currentTimeMillis());
        claimJpaRepository.save(claim);
    }
}