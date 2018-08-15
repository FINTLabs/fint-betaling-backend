package no.fint.betaling.controller

import no.fint.betaling.model.Kunde
import no.fint.betaling.service.CustomerService
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class CustomerControllerSpec extends MockMvcSpecification {//TODO: finn ut hvordan man legger til HTTP headere
    private MockMvc mockMvc
    private CustomerController customerController
    private CustomerService customerService

    void setup() {
        customerService = Mock(CustomerService)
        customerController = new CustomerController(customerService: customerService)
        mockMvc = standaloneSetup(customerController)
    }

    def "Get all customers"() {
        when:
        def response = mockMvc.perform(get('/api/customer'))

        then:
        1 * customerService.getCustomers(_, _) >> [new Kunde(navn: new Personnavn(etternavn: 'Testesen'))]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn.etternavn', 'Testesen'))
    }

    def "Get customers given filter keyword returns filtered list"() {
        when:
        def response = mockMvc.perform(get('/api/customer').param('etternavn', 'r'))

        then:
        1 * customerService.getCustomers(_, _) >> [new Kunde(navn: new Personnavn(etternavn: 'Rettsen'))]
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn.etternavn', 'Rettsen'))
    }
}
