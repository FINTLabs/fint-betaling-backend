package no.fint.betaling.claim;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimStatusHistory;
import org.springframework.stereotype.Service;

@Service
public class ClaimStatusService {

    private final ClaimStatusHistoryJpaRepository claimStatusHistoryJpaRepository;

    public ClaimStatusService(ClaimStatusHistoryJpaRepository claimStatusHistoryJpaRepository) {
        this.claimStatusHistoryJpaRepository = claimStatusHistoryJpaRepository;
    }

    public void setClaimStatusAndSaveHistory(Claim claim, ClaimStatus status) {
        claim.setClaimStatus(status);
        claimStatusHistoryJpaRepository.save(new ClaimStatusHistory(claim, status));
    }

}
