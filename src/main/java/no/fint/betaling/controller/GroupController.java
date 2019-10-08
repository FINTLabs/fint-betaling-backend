package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.KundeGruppe;
import no.fint.betaling.service.GroupService;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/group")
public class GroupController {

    private final CacheManager cacheManager;

    private final GroupService groupService;

    public GroupController(CacheManager cacheManager, GroupService groupService) {
        this.cacheManager = cacheManager;
        this.groupService = groupService;
    }

    //TODO Change to @PathVariable

    @GetMapping("/skole")
    public KundeGruppe getCustomerGroupBySchool(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupBySchool(orgId, schoolId);
    }

    @GetMapping("basisgruppe")
    public List<KundeGruppe> getCustomerGroupsByBasisGroup(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                           @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByBasisGroup(orgId, schoolId);
    }

    @GetMapping("undervisningsgruppe")
    public List<KundeGruppe> getCustomerGroupsByTeachingGroup(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                              @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByTeachingGroup(orgId, schoolId);
    }

    @GetMapping("kontaktlarergruppe")
    public List<KundeGruppe> getCustomerGroupsByContactTeacherGroup(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                                    @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByContactTeacherGroup(orgId, schoolId);
    }
}
