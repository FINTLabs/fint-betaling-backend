package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Repository
public class ClaimRepository {

    private final ClaimJpaRepository claimJpaRepository;

    public ClaimRepository(ClaimJpaRepository claimJpaRepository) {
        this.claimJpaRepository = claimJpaRepository;
    }

    public Claim get(long orderNumber) {
        return claimJpaRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + orderNumber));
    }

    public List<Claim> get(ClaimStatus... statuses) {
        return claimJpaRepository.findByClaimStatusIn(statuses);
    }

    public Claim storeClaim(Claim claim) {
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

    public List<Claim> getByDateAndSchoolAndStatus(LocalDateTime date, String organisationNumber, ClaimStatus[] statuses) {
        String orgNumber = StringUtils.hasText(organisationNumber) ? organisationNumber : null;
        List<ClaimStatus> statusList = (statuses != null && statuses.length > 0) ? Arrays.asList(statuses) : null;
        return claimJpaRepository.getByDateAndSchoolAndStatus(date, orgNumber, statusList);
    }

    public void save(Claim claim) {
        claimJpaRepository.save(claim);
    }
}