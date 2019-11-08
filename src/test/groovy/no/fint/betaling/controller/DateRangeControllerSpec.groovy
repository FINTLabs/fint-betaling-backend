package no.fint.betaling.controller

import no.fint.test.utils.MockMvcSpecification

class DateRangeControllerSpec extends MockMvcSpecification {
    def "Get date ranges returns list"() {
        given:
        def controller = new DateRangeController(dateRanges: ['7', '14', '30'])
        def mockMvc = standaloneSetup(controller)

        when:
        def response = mockMvc.perform(get('/api/date-range'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 3))
                .andExpect(jsonPathEquals('$[0]', '7'))
    }
}
