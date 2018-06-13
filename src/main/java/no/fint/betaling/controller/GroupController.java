package no.fint.betaling.controller;

import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api")
public class GroupController {


    @Autowired
    private GroupService groupService;

    @GetMapping("/group")
    public ResponseEntity getCustomerGroups(){
        List<KundeGruppe> customerGroups = groupService.getCustomerGroups();
        return ResponseEntity.ok(customerGroups);
    }
}
