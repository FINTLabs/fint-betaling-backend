package no.fint.betaling.controller

import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.fint.betaling.model.Customer
import no.fint.betaling.model.Order
import no.fint.betaling.service.ClaimService
import no.fint.betaling.service.ScheduleService
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class ClaimControllerSpec extends Specification {
//
//    private ClaimController controller
//
//    @SpringBean
//    private ClaimService claimService = Mock(ClaimService.class)
//
//    @SpringBean
//    private ScheduleService scheduleService = Mock(ScheduleService.class)
//
//
//    void setup() {
//        controller = new ClaimController(claimService, scheduleService)
//    }
//
//
//    def "Get all payments"() {
//        given:
//        def claim = createClaim(123L)
//
//        when:
//        def response = controller.getAllClaims('', '', null)
//
//        then:
//        1 * claimService.getClaims(_, _, _) >> [claim]
//        response.statusCode == HttpStatus.OK
//        response.getBody() == [claim]
//    }
//
//    def "Set payment given valid payment returns status ok"() {
//
//        when:
//        def response = controller.storeClaim(new Order())
//
//        then:
//        1 * claimService.storeClaims(_ as Order)
//        response.statusCode == HttpStatus.CREATED
//    }
//
//    def "Get payment by name given lastname returns list of payments with matching lastname"() {
//        given:
//        def claim = createClaim(123L)
//
//        when:
//        def response = controller.getClaimsByCustomerName('Testesen')
//
//        then:
//        1 * claimService.getClaimsByCustomerName('Testesen') >> [claim]
//        response.statusCode == HttpStatus.OK
//        response.getBody() == [claim]
//    }
//
//    def "Get payment by orderNumber given valid orderNumber returns list of payments with matching orderNumber"() {
//        given:
//        def claim = createClaim(123L)
//
//        when:
//        def response = controller.getClaimsByOrderNumber(123L)
//
//        then:
//        1 * claimService.getClaimByOrderNumber(123L) >> claim
//        response.statusCode == HttpStatus.OK
//        response.getBody() == claim
//    }
//
//    def "Send invoices given valid org id sends invoices"() {
//        when:
//        def response = controller.sendClaims(['123', '654'])
//
//        then:
//        1 * claimService.sendClaims(["123", "654"] as List)
//        response.statusCode == HttpStatus.CREATED
//
//    }
//
//    def "Get a total count of invoices based on status and on number of days since created"() {
//        given:
//        int amountOfClaims = 78
//
//        when:
//        def response = controller.getCountByStatus(new String[] {ClaimStatus.ERROR.toString()}, '14')
//
//        then:
//        1 * claimService.countClaimsByStatus([ClaimStatus.ERROR] as ClaimStatus[], '14') >> amountOfClaims
//        response.getStatusCode() == HttpStatus.OK
//        response.getBody() == amountOfClaims
//    }
//
//    private static Claim createClaim(long orderNumber) {
//        return new Claim(orderNumber: orderNumber)
//    }
}
