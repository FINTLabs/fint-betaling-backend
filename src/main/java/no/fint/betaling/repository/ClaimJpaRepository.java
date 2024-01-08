package no.fint.betaling.repository;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClaimJpaRepository extends JpaRepository<Claim, Long> {

    @Query("SELECT MAX(c.orderNumber) FROM Claim c")
    Optional<Long> findHighestOrderNumber();

    @Query("SELECT c FROM Claim c WHERE c.claimStatus IN :statuses")
    List<Claim> findByClaimStatusIn(@Param("statuses") ClaimStatus... statuses);

    @Query("SELECT c FROM Claim c WHERE lower(c.customer.name) LIKE lower(concat('%', :name, '%'))")
    List<Claim> findByCustomerName(@Param("name") String name);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.claimStatus IN :statuses")
    int countByStatus(@Param("statuses") ClaimStatus... statuses);

    @Query("SELECT COUNT(c) FROM Claim c WHERE FUNCTION('DATEDIFF', DAY, CURRENT_DATE, c.createdDate) < :days AND c.claimStatus IN :statuses")
    int countByStatusAndDays(@Param("days") long days, @Param("statuses") ClaimStatus[] statuses);

    @Query("SELECT c FROM Claim c WHERE (cast(:date as timestamp) IS NULL OR c.createdDate >= :date) AND (:statuses IS NULL OR c.claimStatus IN :statuses) AND (:organisationNumber IS NULL OR c.organisationUnit.organisationNumber = :organisationNumber)")
    List<Claim> getByDateAndSchoolAndStatus(@Param("date") LocalDateTime date, @Param("organisationNumber") String organisationNumber, @Param("statuses") List<ClaimStatus> statuses);


// Todo: Finn ut hvordan vi alltid kan oppdatere denne
//    public void updateClaim(Query query, Update update) {
//        update.set("timestamp", System.currentTimeMillis());
//        mongoTemplate.upsert(query, update, no.fint.betaling.model.Claim.class);
//    }
}
