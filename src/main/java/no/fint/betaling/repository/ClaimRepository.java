package no.fint.betaling.repository;

import jakarta.annotation.PostConstruct;
import no.fint.betaling.config.Endpoints;
import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ClaimRepository {

    private final Endpoints endpoints;

    private final ClaimJpaRepository claimJpaRepository;

    private static final String ORG_ID = "orgId";

    @Value("${fint.betaling.org-id}")
    private String orgId;

    private final AtomicLong orderNumberCounter = new AtomicLong(100000L);

    public ClaimRepository(Endpoints endpoints, ClaimJpaRepository claimJpaRepository) {
        this.endpoints = endpoints;
        this.claimJpaRepository = claimJpaRepository;
    }

    public Claim get(long orderNumber) {
        throw new NotImplementedException();
        // todo trond: implement
//        return claimJpaRepository.findById(orderNumber)
//                .orElseThrow(() -> new RuntimeException("Claim not found: " + orderNumber));
    }

    public List<Claim> get(ClaimStatus... statuses) {
        return claimJpaRepository.findByClaimStatusIn(statuses);
    }

    public Claim storeClaim(Claim claim) {
        claim.setOrderNumber(orderNumberCounter.incrementAndGet());
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

//    public Query queryByClaimStatusByDays(long days, ClaimStatus... statuses) {
//        LocalDateTime date = LocalDateTime.now().plusDays(-1 * days);
//
//        return createQuery()
//                .addCriteria(Criteria.where("claimStatus").in(Arrays.asList(statuses)))
//                .addCriteria(Criteria.where("lastModifiedDate").gte(date));
//    }
//
//    public Query queryByCustomerNameRegex(String name) {
//        return createQuery().addCriteria(Criteria.where("customer.name").regex(name, "i"));
//    }
//
//    public Query queryByDateAndSchoolAndStatus(Date date, String schoolOrganisationNumber, ClaimStatus... statuses) {
//        Query query = createQuery();
//
//        if (date != null)
//            query.addCriteria(Criteria.where("createdDate").gte(date));
//        if (StringUtils.isNotBlank(schoolOrganisationNumber))
//            query.addCriteria(Criteria.where("organisationUnit.organisationNumber").is(schoolOrganisationNumber));
//        if (statuses != null && statuses.length > 0)
//            query.addCriteria(Criteria.where("claimStatus").in(Arrays.asList(statuses)));
//
//        return query;
//    }


    @PostConstruct
    public void setHighestOrderNumber() {
        orderNumberCounter.set( Long.parseLong(claimJpaRepository.findHighestOrderNumber().orElse("10000").toString()));
    }

    public int countByStatusAndDays(long days, ClaimStatus[] statuses) {
        return claimJpaRepository.countByStatusAndDays(days, statuses);
    }

    public List<Claim> getByDateAndSchoolAndStatus(Date date, String organisationNumber, ClaimStatus[] statuses) {
        return claimJpaRepository.getByDateAndSchoolAndStatus(date, organisationNumber, statuses);
    }

    public void save(Claim claim) {
        claimJpaRepository.save(claim);
    }
}