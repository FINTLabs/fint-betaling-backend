package no.fint.betaling.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.fint.betaling.config.Config.DEFAULT_SCHOOL_ORG_ID;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/school")
    public CustomerGroup getCustomerGroupBySchool(@RequestHeader(name = "x-school-org-id",
            defaultValue = DEFAULT_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupBySchool(schoolId);
    }

    @GetMapping("/basis-group")
    public List<CustomerGroup> getCustomerGroupsByBasisGroupsAndSchool(@RequestHeader(name = "x-school-org-id",
            defaultValue = DEFAULT_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByBasisGroupsAndSchool(schoolId);
    }

    @GetMapping("/teaching-group")
    public List<CustomerGroup> getCustomerGroupsByTeachingGroupsAndSchool(@RequestHeader(name = "x-school-org-id",
            defaultValue = DEFAULT_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByTeachingGroupsAndSchool(schoolId);
    }

    @GetMapping("/contact-teacher-group")
    public List<CustomerGroup> getCustomerGroupsByContactTeacherGroupsAndSchool(@RequestHeader(name = "x-school-org-id",
            defaultValue = DEFAULT_SCHOOL_ORG_ID) String schoolId) {
        return groupService.getCustomerGroupsByContactTeacherGroupsAndSchool(schoolId);
    }
}
