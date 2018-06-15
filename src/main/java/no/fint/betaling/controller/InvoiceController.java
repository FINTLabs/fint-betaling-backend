package no.fint.betaling.controller;

import no.fint.betaling.model.Fakturagrunnlag;
import no.fint.betaling.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/fakturagrunnlag")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity setFakturagrunnlag(@RequestParam Fakturagrunnlag fakturagrunnlag) {
        invoiceService.saveFakturagrunnlag(fakturagrunnlag);
        return ResponseEntity.ok(fakturagrunnlag);
    }

}
