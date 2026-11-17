package no.fint.betaling.controller

import no.fint.betaling.group.*
import no.fint.betaling.model.Customer
import no.fint.betaling.model.CustomerGroup
import org.springframework.http.HttpStatus
import spock.lang.Specification
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.time.Duration

class GroupControllerSpec extends Specification {

    GroupController groupController

    CustomerGroup customerGroup

    private SchoolGroupService schoolGroupService = Mock()

    private KlasseService klasseService = Mock()

    private TeachingGroupService teachingGroupService = Mock()

    private ContactTeacherGroupService contactTeacherGroupService = Mock()

    void setup() {
        groupController = new GroupController(schoolGroupService, klasseService, teachingGroupService, contactTeacherGroupService)
        customerGroup = new CustomerGroup(name: 'testGroup', description: 'test', customers: [new Customer(name: 'Testesen')])
    }

    def "Get customers from school"() {

        when:
        def response = groupController.getFromSchool('DUMMY').block()

        then:
        1 * schoolGroupService.getFromSchool('DUMMY') >> customerGroup
        response.statusCode == HttpStatus.OK
        response.getBody() == customerGroup
    }

    def "Get customer groups from basis groups"() {

        when:
        def response = groupController.getFromBasisGroups('DUMMY').block()

        then:
        1 * klasseService.getFromKlasse('DUMMY') >> [customerGroup]
        response.statusCode == HttpStatus.OK
        response.getBody() == [customerGroup]
    }

    def "Get customer groups from teaching groups"() {

        when:
        def response = groupController.getFromTeachingGroups('DUMMY').block()

        then:
        1 * teachingGroupService.getFromTeachingGroups('DUMMY') >> [customerGroup]
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == [customerGroup]
    }

    def "Get customer groups from contact teacher groups"() {

        when:
        def response = groupController.getFromContactTeacherGroups('DUMMY').block()

        then:
        1 * contactTeacherGroupService.getFromContactTeacherGroups('DUMMY') >> [customerGroup]
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == [customerGroup]
    }
    def "group loading can block on a worker even when subscribed from a Reactor parallel thread"() {
        given:
        schoolGroupService.getFromSchool('DUMMY') >> {
            assert !Schedulers.isInNonBlockingThread()
            Mono.just(customerGroup).delayElement(Duration.ofMillis(10)).block()
        }

        expect:
        StepVerifier.create(groupController.getFromSchool('DUMMY').subscribeOn(Schedulers.parallel()))
                .assertNext { assert it.body == customerGroup }
                .verifyComplete()
    }
}