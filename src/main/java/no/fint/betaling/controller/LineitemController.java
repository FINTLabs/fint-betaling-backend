package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.repository.LineitemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/lineitem")
public class LineitemController {

    @Autowired
    private LineitemRepository repository;

    @GetMapping
    public Collection<Lineitem> getAllLineitems() {
        return repository.getLineitems();
    }
}
