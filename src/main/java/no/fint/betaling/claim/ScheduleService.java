package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleService {

    private final ClaimService claimService;

    public ScheduleService(ClaimService claimService) {
        this.claimService = claimService;
    }

    /*
    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.invoice-send-rate}")
    public void sendClaims(){
        log.info("Sending claims...");
        try {
            claimService.sendClaims();
        } catch (Exception e) {
            log.error("Error caught when sending claims!", e);
        }
    }
    */

    @Scheduled(initialDelay = 60000, fixedRateString = "${fint.betaling.invoice-update-rate}")
    public void updateRecentlySentClaims() {
        log.debug("Updating sent claims...");
        try {
            claimService.updateSentClaims();
        } catch (Exception e) {
            log.error("Error caught when updating sent claims!", e);
        }
    }

    @Scheduled(cron = "${fint.betaling.invoice-update-cron}")
    public void updateAcceptedClaims() {
        log.debug("Updating accepted claims...");
        try {
            claimService.updateAcceptedClaims();
        } catch (Exception e) {
            log.error("Error caught when updating claims!", e);
        }
    }
}
