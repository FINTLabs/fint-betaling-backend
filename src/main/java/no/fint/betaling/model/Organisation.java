package no.fint.betaling.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "organisation")
public class Organisation {
    private String name;
    @Id
    private String organisationNumber;
}
