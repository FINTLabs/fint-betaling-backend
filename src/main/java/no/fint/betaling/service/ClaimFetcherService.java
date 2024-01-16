package no.fint.betaling.service;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimsDatePeriod;
import no.fint.betaling.repository.ClaimRepository;

import java.util.List;

import static no.fint.betaling.service.ClaimService.claimsDatePeriodToTimestamp;

public class ClaimFetcherService {

    private final ClaimRepository claimRepository;

    public ClaimFetcherService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    public List<Claim> getClaimsByPeriodAndOrganisationnumberAndStatus(ClaimsDatePeriod period, String organisationNumber, ClaimStatus[] statuses) {
        return claimRepository.getByDateAndSchoolAndStatus(
                claimsDatePeriodToTimestamp(period),
                organisationNumber,
                statuses);
    }

    public List<Claim> getAcceptedClaims() {
        return claimRepository.get(
                ClaimStatus.ACCEPTED,
                ClaimStatus.ISSUED,
                // ClaimStatus.PAID,  // TODO Used to be workaround for issue with Visma Fakturering
                ClaimStatus.UPDATE_ERROR);
    }

    public List<Claim> getClaimsByStatus(ClaimStatus[] statuses) {
        return claimRepository.get(statuses);
    }

    public Claim getClaimByOrderNumber(long orderNumber) {
        return claimRepository.get(orderNumber);
    }

    public List<Claim> getSentClaims() {
        return claimRepository.get(
                ClaimStatus.SENT);
    }

    public List<Claim> getClaimsByCustomerName(String name) {
        return claimRepository.getByCustomerName(name);
    }

    public List<Claim> getUnsentClaims() {
        return claimRepository.get(
                ClaimStatus.STORED,
                ClaimStatus.SEND_ERROR);
    }

}
