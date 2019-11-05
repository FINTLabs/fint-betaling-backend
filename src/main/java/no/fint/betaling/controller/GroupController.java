package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.HeaderConstants.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/skole")
    public CustomerGroup getCustomerGroupBySchool(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                  @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupBySchool(orgId, schoolId);
    }

    @GetMapping("basisgruppe")
    public List<CustomerGroup> getCustomerGroupsByBasisGroup(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                             @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByBasisGroup(orgId, schoolId);
    }

    @GetMapping("undervisningsgruppe")
    public List<CustomerGroup> getCustomerGroupsByTeachingGroup(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                                @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByTeachingGroup(orgId, schoolId);
    }

    @GetMapping("kontaktlarergruppe")
    public List<CustomerGroup> getCustomerGroupsByContactTeacherGroup(@RequestHeader(name = ORG_ID, defaultValue = DEFAULT_VALUE_ORG_ID) String orgId,
                                                                      @RequestHeader(name = SCHOOL_ORG_ID, defaultValue = DEFAULT_VALUE_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByContactTeacherGroup(orgId, schoolId);
    }
}
