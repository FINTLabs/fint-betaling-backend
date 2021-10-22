package no.fint.betaling.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * There is a bug in Visma Enterprise that cause the transfer to fail if orders are sent within the same second.
 * This is meant as a temporary workaround until Visma has fixed the bug on their side.
 */
@Slf4j
@Component
public class DelayOrderSending {

    @Value("${fint.betaling.wait-between-orders:0}")
    private long delayBetweenOrders;

    private long previousTime;

    public void sleepIfNecessary() {
        if (delayBetweenOrders <= 0) return;
        long currentTime = System.currentTimeMillis();
        long millisSincePrevious = currentTime - previousTime;

        if (millisSincePrevious < delayBetweenOrders){
            sleep(delayBetweenOrders - millisSincePrevious);
        }

        previousTime = System.currentTimeMillis();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
    }
}
