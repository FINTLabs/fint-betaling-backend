package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleService {

    private final ClaimRestService claimRestService;

    public ScheduleService(ClaimRestService claimRestService) {
        this.claimRestService = claimRestService;
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
        // Deactivated, try trigger based on event instead (see ClaimRestStatusService)
//        log.debug("Updating sent claims...");
//        try {
//            claimRestService.updateSentClaims();
//        } catch (Exception e) {
//            log.error("Error caught when updating sent claims!", e);
//        }
    }

    @Scheduled(cron = "${fint.betaling.invoice-update-cron}")
    public void updateAcceptedClaims() {
        log.debug("Updating accepted claims...");
        try {
            claimRestService.updateAcceptedClaims();
        } catch (Exception e) {
            log.error("Error caught when updating claims!", e);
        }
    }
}
