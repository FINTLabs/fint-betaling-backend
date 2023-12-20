package no.fint.betaling.model;

import com.google.common.util.concurrent.AtomicDouble;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "creditnote")
public class CreditNote {

    private String orderNumber;

    // todo: Is id uniq for each creditnote or for all creditnotes?
    @Id
    private String id;

    private LocalDate date;

    private Long amount;

    private String comment;
}
