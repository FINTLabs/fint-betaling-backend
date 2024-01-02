package no.fint.betaling.controller

import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@TestPropertySource(properties = "fint.betaling.date-range=7, 14, 30")
class DateRangeControllerSpec extends Specification {

    def "Get date ranges returns array"() {
        given:
        // Todo: this didn't work, so added the value as default to the field
        //def controller = new DateRangeController(dateRanges: ["7", "14", "30"] as String[])
        def controller = new DateRangeController()

        when:
        def response = controller.getDateRange()

        then:
        response.statusCode == HttpStatus.OK
        response.getBody() == ["7", "14", "30"]
    }
}
