package no.fint.betaling.controller;

import no.fint.betaling.model.Taxcode;
import no.fint.betaling.repository.TaxcodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/mva-code")
public class TaxcodeController {

    @Autowired
    private TaxcodeRepository repository;

    @GetMapping
    public Collection<Taxcode> getMvaCodes() {
        return repository.getTaxcodes();
    }
}
