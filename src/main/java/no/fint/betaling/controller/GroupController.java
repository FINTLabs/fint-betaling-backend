package no.fint.betaling.controller;

import no.fint.betaling.config.HeaderConstants;
import no.fint.betaling.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/group")
public class GroupController {


    @Autowired
    private GroupService groupService;

    @GetMapping
    public ResponseEntity getAllCustomerGroups(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        return ResponseEntity.ok(groupService.getAllCustomerGroups(orgId));
    }

    @GetMapping("/basisgruppe")
    public ResponseEntity getCustomerGroupsFromBasisgruppe(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        return ResponseEntity.ok(groupService.getCustomerGroupListFromBasisgruppe(orgId));
    }

    @GetMapping("/undervisningsgruppe")
    public ResponseEntity getCustomerGroupsFromFag(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        return ResponseEntity.ok(groupService.getCustomerGroupListFromUndervisningsgruppe(orgId));
    }

    @GetMapping("/kontaktlarergruppe")
    public ResponseEntity getCustomerGroupListFromKontaktlarergruppe(@RequestHeader(name = HeaderConstants.ORG_ID, defaultValue = "${fint.betaling.default-org-id}", required = false) String orgId) {
        return ResponseEntity.ok(groupService.getCustomerGroupListFromKontaktlarergruppe(orgId));
    }

}
