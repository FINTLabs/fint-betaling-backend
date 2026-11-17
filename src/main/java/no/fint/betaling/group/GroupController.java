package no.fint.betaling.group;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.CustomerGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/group")
public class GroupController {

    private final SchoolGroupService schoolGroupService;

    private final KlasseService klasseService;

    private final TeachingGroupService teachingGroupService;

    private final ContactTeacherGroupService contactTeacherGroupService;

    public GroupController(SchoolGroupService schoolGroupService, KlasseService klasseService, TeachingGroupService teachingGroupService, ContactTeacherGroupService contactTeacherGroupService) {
        this.schoolGroupService = schoolGroupService;
        this.klasseService = klasseService;
        this.teachingGroupService = teachingGroupService;
        this.contactTeacherGroupService = contactTeacherGroupService;
    }

    @GetMapping("/school")
    public Mono<ResponseEntity<CustomerGroup>> getFromSchool(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return Mono.fromCallable(() -> ResponseEntity.ok(schoolGroupService.getFromSchool(schoolId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/basis-group")
    public Mono<ResponseEntity<List<CustomerGroup>>> getFromBasisGroups(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return Mono.fromCallable(() -> ResponseEntity.ok(klasseService.getFromKlasse(schoolId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/teaching-group")
    public Mono<ResponseEntity<List<CustomerGroup>>> getFromTeachingGroups(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return Mono.fromCallable(() -> ResponseEntity.ok(teachingGroupService.getFromTeachingGroups(schoolId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/contact-teacher-group")
    public Mono<ResponseEntity<List<CustomerGroup>>> getFromContactTeacherGroups(@RequestHeader(name = "x-school-org-id") String schoolId) {
        return Mono.fromCallable(() -> ResponseEntity.ok(contactTeacherGroupService.getFromContactTeacherGroups(schoolId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
