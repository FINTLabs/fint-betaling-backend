package no.fint.betaling.controller

import no.fint.betaling.model.Kunde
import no.fint.betaling.service.GroupService
import no.fint.betaling.service.StudentService
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class StudentControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private StudentController studentController
    private StudentService studentService
    private GroupService groupService

    void setup() {
        groupService = Mock(GroupService)
        studentService = Mock(StudentService)
        studentController = new StudentController(studentService: studentService, groupService: groupService)
        mockMvc = standaloneSetup(studentController)
    }

    def "Get all customers"() {
        when:
        def response = mockMvc.perform(get('/api/customers'))

        then:
        1 * studentService.getCustomers() >> [new Kunde(navn: new Personnavn(etternavn: 'Testesen'))]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn.etternavn','Testesen'))
    }
}
