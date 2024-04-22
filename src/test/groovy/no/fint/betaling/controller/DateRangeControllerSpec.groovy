package no.fint.betaling.controller

import no.fint.betaling.claim.DateRangeController
import org.springframework.http.HttpStatus
import spock.lang.Specification

class DateRangeControllerSpec extends Specification {

    def "Get date ranges returns array"() {
        given:
        def controller = new DateRangeController()
        controller.setDateRange("7", "14", "30")

        when:
        def response = controller.getDateRange()

        then:
        response.statusCode == HttpStatus.OK
        response.getBody() == ["7", "14", "30"]
    }
}
