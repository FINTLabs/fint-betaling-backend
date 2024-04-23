package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.ClientErrorException;
import no.fint.betaling.common.exception.ServerErrorException;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class ClaimRestStatusService {
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(10);
    private static final int MAX_ATTEMPTS = 120;
    private static final Duration MAX_TOTAL_DURATION = Duration.ofMinutes(20);

    private final RestUtil restClient;
    private final ClaimRepository claimRepository;

    public ClaimRestStatusService(RestUtil restClient, ClaimRepository claimRepository) {
        this.restClient = restClient;
        this.claimRepository = claimRepository;
    }

    @Async
    public void processRequest(Claim claim) {

        String url = claim.getInvoiceUri();
        if (!isValidStatusUri(url)) {
            log.error("Invalid status uri for claim {} with url {}", claim.getOrderNumber(), url);
            return;
        }

        restClient.bodyless(url)
                .flatMap(response -> evaluateResponse(response, claim))
                .repeatWhenEmpty(repeat -> repeat.delayElements(INITIAL_DELAY).take(MAX_ATTEMPTS))
                .retryWhen(Retry
                        .backoff(MAX_ATTEMPTS, INITIAL_DELAY)
                        .maxBackoff(MAX_TOTAL_DURATION)
                        .filter(this::isRetryableException))
                .subscribe(
                        response -> updateClaimOnSuccess(claim, response),
                        error -> updateClaimOnFailure(claim, error)
                );
    }

    private void updateClaimOnSuccess(Claim claim, ResponseEntity<Void> response) {

        String location = getLocation(response);

        if (!StringUtils.hasText(location)) {
            log.error("Unexpected result: Success on getting status, but Location is empty for claim {} with url {} gave response {}", claim.getOrderNumber(), claim.getInvoiceUri(), response.getStatusCode());
            claim.setClaimStatus(ClaimStatus.SEND_ERROR);
            claim.setStatusMessage("Successfull return but location is empty");
            claimRepository.save(claim);
            return;
        }

        log.info("Claim {} created ({}), location: {}", claim.getOrderNumber(), response.getStatusCode(), location);
        claim.setInvoiceUri(location);
        claim.setClaimStatus(ClaimStatus.ACCEPTED);
        claim.setStatusMessage(null);
        claimRepository.save(claim);
    }

    private void updateClaimOnFailure(Claim claim, Throwable error) {
        log.warn("Error updating claim {} {} with invoiceUri {}", claim.getOrderNumber(), claim.getClaimStatus(), claim.getInvoiceUri());
        log.error("Error in checking claim status: " + error.getMessage(), error);

        claim.setClaimStatus(ClaimStatus.SEND_ERROR);
        claim.setStatusMessage(error.getMessage());
        claim.setInvoiceUri(null);
        claimRepository.save(claim);
    }

    private Mono<ResponseEntity<Void>> evaluateResponse(ResponseEntity<Void> response, Claim claim) {
        HttpStatusCode statusCode = response.getStatusCode();

        if (statusCode.equals(HttpStatus.ACCEPTED)) {
            return Mono.empty(); // Retur empty Mono to trigger repeatWhenEmpty
        } else if (statusCode.is2xxSuccessful()) {
            return Mono.just(response); // Return response to break retryWhen
        } else if (statusCode.is4xxClientError()) {
            return Mono.error(new ClientErrorException(statusCode));
        } else if (statusCode.is5xxServerError()) {
            return Mono.error(new ServerErrorException(statusCode));
        } else {
            return Mono.error(new RuntimeException("Unexpected status code: " + statusCode));
        }
    }

    private String getLocation(ResponseEntity<Void> response) {
        if (response.getHeaders().getLocation() == null) return "";
        return response.getHeaders().getLocation().toString();
    }

    private boolean isRetryableException(Throwable ex) {
        return ex instanceof ServerErrorException;
    }

    private boolean isValidStatusUri(String url) {
        if (!StringUtils.hasText(url)) return false;
        return url.contains("status");
    }
}
