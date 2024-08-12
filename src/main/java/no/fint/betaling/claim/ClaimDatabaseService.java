package no.fint.betaling.claim;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import no.fint.betaling.model.ClaimsDatePeriod;
import no.fint.betaling.model.Order;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimDatabaseService {

    private final ClaimRepository claimRepository;
    private final ClaimFactory claimFactory;

    public ClaimDatabaseService(ClaimRepository claimRepository,
                                ClaimFactory claimFactory) {
        this.claimRepository = claimRepository;
        this.claimFactory = claimFactory;
    }

    public List<Claim> storeClaims(Order order) {
        return claimFactory
                .createClaims(order)
                .stream()
                .map(claimRepository::storeClaim)
                .collect(Collectors.toList());
    }

    public int countClaimsByStatus(ClaimStatus[] statuses, String days) {
        if (StringUtils.isNotBlank(days)) {
            return claimRepository.countByStatusAndDays(Long.parseLong(days), statuses);
        }
        return claimRepository.countByStatus(statuses);
    }

    public void cancelClaim(long orderNumber) {
        Claim claim = getClaimByOrderNumber(orderNumber);

        if (claim.getClaimStatus().equals(ClaimStatus.STORED)) {
            claim.setClaimStatus(ClaimStatus.CANCELLED);
            claimRepository.save(claim);
        } else {
            log.warn("cancel claim called, but claim was not in stored status (orderNumber: {}, status: {})", orderNumber, claim.getClaimStatus());
        }
    }

    public List<Claim> getClaimsByPeriodAndOrganisationnumberAndStatus(ClaimsDatePeriod period, String organisationNumber, ClaimStatus[] statuses) {
        return claimRepository.getByDateAndSchoolAndStatus(
                claimsDatePeriodToTimestamp(period),
                organisationNumber,
                statuses);
    }

    public List<Claim> getClaimsByStatus(ClaimStatus... statuses) {
        return claimRepository.get(statuses);
    }

    public Claim getClaimByOrderNumber(long orderNumber) {
        return claimRepository.get(orderNumber);
    }

    public List<Claim> getClaimsByCustomerName(String name) {
        return claimRepository.getByCustomerName(name);
    }

    public List<Claim> getUnsentClaims() {
        return claimRepository.get(
                ClaimStatus.STORED,
                ClaimStatus.SEND_ERROR);
    }

    public static LocalDateTime claimsDatePeriodToTimestamp(ClaimsDatePeriod period) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        switch (period) {
            case ALL:
                break;
            case WEEK:
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                break;
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case YEAR:
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }

        return period == ClaimsDatePeriod.ALL ? null : LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
}