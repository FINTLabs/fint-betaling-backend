package no.fint.betaling.controller

import no.fint.betaling.model.Kunde
import no.fint.betaling.service.StudentService
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class StudentControllerSpec extends MockMvcSpecification {//TODO: finn ut hvordan man legger til HTTP headere
    private MockMvc mockMvc
    private StudentController studentController
    private StudentService studentService

    void setup() {
        studentService = Mock(StudentService)
        studentController = new StudentController(studentService: studentService)
        mockMvc = standaloneSetup(studentController)
    }

    def "Get all customers"() {
        when:
        def response = mockMvc.perform(get('/api/customer'))

        then:
        1 * studentService.getCustomers(_, _) >> [new Kunde(navn: new Personnavn(etternavn: 'Testesen'))]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn.etternavn', 'Testesen'))
    }

    def "Get customers given filter keyword returns filtered list"() {
        when:
        def response = mockMvc.perform(get('/api/customer').param('etternavn', 'r'))

        then:
        1 * studentService.getCustomers(_, _) >> [new Kunde(navn: new Personnavn(etternavn: 'Rettsen'))]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn.etternavn', 'Rettsen'))
    }
}
