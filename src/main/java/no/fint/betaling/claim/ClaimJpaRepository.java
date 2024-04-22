package no.fint.betaling.claim;

import no.fint.betaling.model.Claim;
import no.fint.betaling.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClaimJpaRepository extends JpaRepository<Claim, Long> {

    @Query("SELECT c FROM Claim c WHERE c.claimStatus IN :statuses")
    List<Claim> findByClaimStatusIn(@Param("statuses") ClaimStatus... statuses);

    @Query("SELECT c FROM Claim c WHERE lower(c.customerName) LIKE lower(concat('%', :name, '%'))")
    List<Claim> findByCustomerName(@Param("name") String name);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.claimStatus IN :statuses")
    int countByStatus(@Param("statuses") ClaimStatus... statuses);

    @Query("SELECT COUNT(c) FROM Claim c WHERE FUNCTION('DATEDIFF', DAY, CURRENT_DATE, c.createdDate) < :days AND c.claimStatus IN :statuses")
    int countByStatusAndDays(@Param("days") long days, @Param("statuses") ClaimStatus[] statuses);

    @Query("SELECT c FROM Claim c WHERE (cast(:date as timestamp) IS NULL OR c.createdDate >= :date) AND (:statuses IS NULL OR c.claimStatus IN :statuses) AND (:organisationNumber IS NULL OR c.organisationUnit.organisationNumber = :organisationNumber) ORDER BY c.orderNumber DESC")
    List<Claim> getByDateAndSchoolAndStatus(@Param("date") LocalDateTime date, @Param("organisationNumber") String organisationNumber, @Param("statuses") List<ClaimStatus> statuses);

}
