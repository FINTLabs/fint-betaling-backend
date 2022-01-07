package no.fint.betaling.service;

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

    @Scheduled(cron = "${fint.betaling.invoice-update-cron}")
    public void updateClaims() {
        log.info("Updating claims...");
        try {
            claimService.updateClaims();
        } catch (Exception e) {
            log.error("Error caught when updating claims!", e);
        }
    }
}
