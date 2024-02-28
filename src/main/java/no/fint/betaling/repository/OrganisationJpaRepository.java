package no.fint.betaling.repository;

import no.fint.betaling.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationJpaRepository extends JpaRepository<Organisation, String> {
}
