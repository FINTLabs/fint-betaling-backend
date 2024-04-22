package no.fint.betaling.organisation;

import no.fint.betaling.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationJpaRepository extends JpaRepository<Organisation, String> {
}
