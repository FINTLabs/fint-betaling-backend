package no.fint.betaling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/dato")
public class DateRangeController {

    @Value("${fint.betaling.date-range}")
    private String[] dateRanges;

    @GetMapping
    public ResponseEntity getDateRange() {
        return ResponseEntity.ok(dateRanges);
    }
}
