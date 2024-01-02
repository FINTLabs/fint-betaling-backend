package no.fint.betaling.controller;

import no.fint.betaling.model.Taxcode;
import no.fint.betaling.repository.TaxcodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api/mva-code")
public class TaxcodeController {

    private TaxcodeRepository repository;

    public TaxcodeController(TaxcodeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Collection<Taxcode>> getMvaCodes() {
        return ResponseEntity.ok(repository.getTaxcodes());
    }
}
