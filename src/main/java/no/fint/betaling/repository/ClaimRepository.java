package no.fint.betaling.repository;

import jakarta.annotation.PostConstruct;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ClaimRepository {

    private final ClaimJpaRepository claimJpaRepository;

    private final AtomicLong orderNumberCounter = new AtomicLong(100000L);

    public ClaimRepository(ClaimJpaRepository claimJpaRepository) {
        this.claimJpaRepository = claimJpaRepository;
    }

    @PostConstruct
    public void setHighestOrderNumber() {
        orderNumberCounter.set(Long.parseLong(claimJpaRepository.findHighestOrderNumber().orElse(10000L).toString()));
    }

    public Claim get(long orderNumber) {
        return claimJpaRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + orderNumber));
    }

    public List<Claim> get(ClaimStatus... statuses) {
        return claimJpaRepository.findByClaimStatusIn(statuses);
    }

    public Claim storeClaim(Claim claim) {
        claim.setOrderNumber(orderNumberCounter.incrementAndGet());
        claim.setTimestamp(System.currentTimeMillis());
        claimJpaRepository.save(claim);
        return claim;
    }

    public List<Claim> getAll() {
        return claimJpaRepository.findAll();
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

    public List<Claim> getByDateAndSchoolAndStatus(Date date, String organisationNumber, ClaimStatus[] statuses) {
        if (!StringUtils.hasText(organisationNumber)) organisationNumber = null;
        if (statuses != null && statuses.length == 0) statuses = null;
        return claimJpaRepository.getByDateAndSchoolAndStatus(date, organisationNumber, statuses);
    }

    public void save(Claim claim) {
        claimJpaRepository.save(claim);
    }
}