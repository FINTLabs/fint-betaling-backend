package no.fint.betaling.claim;

import no.fint.betaling.model.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimStatusHistoryJpaRepository extends JpaRepository<ClaimStatusHistory, Long> {
}
