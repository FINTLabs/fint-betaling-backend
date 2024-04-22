package no.fint.betaling.group;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.CustomerGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/school")
    public ResponseEntity<CustomerGroup> getCustomerGroupBySchool(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(groupService.getCustomerGroupBySchool(schoolId));
    }

    @GetMapping("/basis-group")
    public ResponseEntity<List<CustomerGroup>> getCustomerGroupsByBasisGroupsAndSchool(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(groupService.getCustomerGroupsByBasisGroupsAndSchool(schoolId));
    }

    @GetMapping("/teaching-group")
    public ResponseEntity<List<CustomerGroup>> getCustomerGroupsByTeachingGroupsAndSchool(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(groupService.getCustomerGroupsByTeachingGroupsAndSchool(schoolId));
    }

    @GetMapping("/contact-teacher-group")
    public ResponseEntity<List<CustomerGroup>> getCustomerGroupsByContactTeacherGroupsAndSchool(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(groupService.getCustomerGroupsByContactTeacherGroupsAndSchool(schoolId));
    }
}
