package no.fint.betaling.controller;

import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.DEFAULT_VALUE_ORG_ID;
import static no.fint.betaling.config.HeaderConstants.ORG_ID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/group")
public class GroupController {


    @Autowired
    private GroupService groupService;

    @GetMapping
    public List<KundeGruppe> getAllCustomerGroups(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return groupService.getAllCustomerGroups(orgId);
    }

    @GetMapping("/basisgruppe")
    public List<KundeGruppe> getCustomerGroupsFromBasisgruppe(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return groupService.getCustomerGroupListFromBasisgruppe(orgId);
    }

    @GetMapping("/undervisningsgruppe")
    public List<KundeGruppe> getCustomerGroupsFromFag(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return groupService.getCustomerGroupListFromUndervisningsgruppe(orgId);
    }

    @GetMapping("/kontaktlarergruppe")
    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId) {
        return groupService.getCustomerGroupListFromKontaktlarergruppe(orgId);
    }

}
