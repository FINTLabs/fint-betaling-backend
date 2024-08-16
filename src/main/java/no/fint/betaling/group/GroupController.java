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

    private final SchoolGroupService schoolGroupService;

    private final BasisGroupService basisGroupService;

    private final TeachingGroupService teachingGroupService;

    private final ContactTeacherGroupService contactTeacherGroupService;

    public GroupController(SchoolGroupService schoolGroupService, BasisGroupService basisGroupService, TeachingGroupService teachingGroupService, ContactTeacherGroupService contactTeacherGroupService) {
        this.schoolGroupService = schoolGroupService;
        this.basisGroupService = basisGroupService;
        this.teachingGroupService = teachingGroupService;
        this.contactTeacherGroupService = contactTeacherGroupService;
    }

    @GetMapping("/school")
    public ResponseEntity<CustomerGroup> getFromSchool(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(schoolGroupService.getFromSchool(schoolId));
    }

    @GetMapping("/basis-group")
    public ResponseEntity<List<CustomerGroup>> getFromBasisGroups(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(basisGroupService.getFromBasisGroups(schoolId));
    }

    @GetMapping("/teaching-group")
    public ResponseEntity<List<CustomerGroup>> getFromTeachingGroups(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(teachingGroupService.getFromTeachingGroups(schoolId));
    }

    @GetMapping("/contact-teacher-group")
    public ResponseEntity<List<CustomerGroup>> getFromContactTeacherGroups(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return ResponseEntity.ok(contactTeacherGroupService.getFromContactTeacherGroups(schoolId));
    }
}
