package no.fint.betaling.controller;

import no.fint.betaling.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/group")
public class GroupController {


    @Autowired
    private GroupService groupService;

    @GetMapping
    public ResponseEntity getAllCustomerGroups() {
        return ResponseEntity.ok(groupService.getAllCustomerGroups());
    }

    @GetMapping("/basisgruppe")
    public ResponseEntity getCustomerGroupsFromBasisgruppe() {
        return ResponseEntity.ok(groupService.getCustomerGroupListFromBasisgruppe());
    }

    @GetMapping("/undervisningsgruppe")
    public ResponseEntity getCustomerGroupsFromFag() {
        return ResponseEntity.ok(groupService.getCustomerGroupListFromUndervisningsgruppe());
    }

    @GetMapping("/kontaktlarergruppe")
    public ResponseEntity getCustomerGroupListFromKontaktlarergruppe() {
        return ResponseEntity.ok(groupService.getCustomerGroupListFromKontaktlarergruppe());
    }

}
