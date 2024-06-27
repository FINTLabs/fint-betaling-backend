package no.fint.betaling.taxcode;

import no.fint.betaling.fintdata.TaxCodeRepository;
import no.fint.betaling.model.Taxcode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(value = "/mva-code")
public class TaxcodeController {

    private final TaxCodeRepository repository;

    public TaxcodeController(TaxCodeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Collection<Taxcode>> getMvaCodes() {
        return ResponseEntity.ok(repository.getTaxcodes());
    }
}
