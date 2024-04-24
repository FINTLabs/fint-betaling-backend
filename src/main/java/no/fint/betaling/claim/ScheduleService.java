package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class ScheduleService {

    private final ClaimRestService claimRestService;

    public ScheduleService(ClaimRestService claimRestService) {
        this.claimRestService = claimRestService;
    }

    @Scheduled(cron = "0 48 6-16 * * MON-FRI")
    public void updateAcceptedClaims() {
        updateClaims(ClaimStatus.ACCEPTED, Duration.ofDays(30));
    }

    @Scheduled(cron = "0 30 16 ? * MON-FRI")
    public void updateIssuedClaims() {
        updateClaims(ClaimStatus.ISSUED, Duration.ofDays(100));
    }

    @Scheduled(cron = "0 38 8,10,12,14 * * MON-FRI")
    public void updateUpdateErrorClaims() {
        updateClaims(ClaimStatus.UPDATE_ERROR, Duration.ofDays(30));
    }

    private void updateClaims(ClaimStatus claimStatus, Duration maxAge) {
        log.debug("Updating claims " + claimStatus);
        try {
            claimRestService.updateClaims(claimStatus, maxAge);
        } catch (Exception e) {
            log.error("Error caught when updating claims!", e);
        }
    }
}
