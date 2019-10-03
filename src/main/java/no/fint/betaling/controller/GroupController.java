package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.ResourceNotFoundException;
import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.*;

@Slf4j
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

    @GetMapping("/skole")
    public KundeGruppe getCustomerGroupFromSchool(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                  @RequestHeader(name = SCHOOL_ORG_ID) String schoolOrgId) {
        KundeGruppe customerGroup = groupService.getCustomerGroupFromSchool(orgId, schoolOrgId);
        log.info("Returning {} students", customerGroup.getKundeliste().size());
        return customerGroup;
        //return groupService.getCustomerGroupFromSchool(orgId, schoolOrgId);
    }

    @GetMapping("/basisgruppe")
    public List<KundeGruppe> getCustomerGroupsFromBasisgruppe(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                              @RequestHeader(name = SCHOOL_ORG_ID) String schoolOrgId) {
        List<KundeGruppe> customerGroups = groupService.getCustomerGroupListFromBasisgruppeAtSchool(orgId, schoolOrgId);
        log.info("Returning {} basis groups", customerGroups.size());
        return customerGroups;
        //return groupService.getCustomerGroupListFromBasisgruppe(orgId);
    }

    @GetMapping("/undervisningsgruppe")
    public List<KundeGruppe> getCustomerGroupsFromFag(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                      @RequestHeader(name = SCHOOL_ORG_ID) String schoolOrgId) {
        List<KundeGruppe> customerGroups = groupService.getCustomerGroupListFromUndervisningsgruppeAtSchool(orgId, schoolOrgId);
        log.info("Returning {} teaching groups", customerGroups.size());
        return customerGroups;
        //return groupService.getCustomerGroupListFromUndervisningsgruppe(orgId);
    }

    @GetMapping("/kontaktlarergruppe")
    public List<KundeGruppe> getCustomerGroupListFromKontaktlarergruppe(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                                        @RequestHeader(name = SCHOOL_ORG_ID) String schoolOrgId) {
        List<KundeGruppe> customerGroups = groupService.getCustomerGroupListFromKontaktLarergruppeAtSchool(orgId, schoolOrgId);
        log.info("Returning {} contact teacher groups", customerGroups.size());
        return customerGroups;
        //return groupService.getCustomerGroupListFromKontaktlarergruppe(orgId);
    }
}
