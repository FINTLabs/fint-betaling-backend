package no.fint.betaling.claim;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Setter
@RestController
@RequestMapping(value = "/date-range")
public class DateRangeController {

    @Value("${fint.betaling.date-range:7, 14, 30}")
    private String[] dateRange;

    @GetMapping
    public ResponseEntity<String[]> getDateRange() {
        return ResponseEntity.ok(dateRange);
    }
}
