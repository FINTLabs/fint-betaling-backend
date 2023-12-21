package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ClaimJpaRepository<Claim, Long> extends JpaRepository<Claim, Long> {

    @Query("SELECT MAX(c.orderNumber) FROM Claim c")
    Optional<Long> findHighestOrderNumber();

    @Query("SELECT c FROM Claim c WHERE c.claimStatus IN :statuses")
    List<Claim> findByClaimStatusIn(@Param("statuses") ClaimStatus... statuses);

    @Query("SELECT c FROM Claim c WHERE c.customer.name = :name")
    List<Claim> findByCustomerName(@Param("name") String name);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.claimStatus IN :statuses")
    int countByStatus(@Param("statuses") ClaimStatus... statuses);

    @Query("SELECT COUNT(c) FROM Claim c WHERE DATEDIFF(CURRENT_DATE, c.createdDate) < :days AND c.claimStatus IN :statuses")
    int countByStatusAndDays(@Param("days") long days, @Param("statuses") ClaimStatus[] statuses);

    @Query("SELECT c FROM Claim c WHERE c.createdDate >= :date AND c.claimStatus IN :statuses AND c.organisationUnit.organisationNumber = :organisationNumber")
    List<Claim> getByDateAndSchoolAndStatus(@Param("date") Date date, @Param("organisationNumber") String organisationNumber, @Param("statuses") ClaimStatus[] statuses);


// Todo: Finn ut hvordan vi alltid kan oppdatere denne
//    public void updateClaim(Query query, Update update) {
//        update.set("timestamp", System.currentTimeMillis());
//        mongoTemplate.upsert(query, update, no.fint.betaling.model.Claim.class);
//    }
}
