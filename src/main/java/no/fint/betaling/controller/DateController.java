package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/dato")
public class DateController {

    //@Value("${fint.betaling.invoice.date-range}")
    private String[] dateRange = {"7", "14", "30"};

    @GetMapping
    public ResponseEntity getDateRange(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "test.no", required = false) String orgId){
        return ResponseEntity.ok(dateRange);
    }
}
