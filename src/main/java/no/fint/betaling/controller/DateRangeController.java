package no.fint.betaling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/date-range")
public class DateRangeController {

    @Value("${fint.betaling.date-range:7, 14, 30}")
    private String[] dateRanges;

    @GetMapping
    public String[] getDateRange() {
        return dateRanges;
    }
}
