package no.fint.betaling.controller

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeGruppe
import no.fint.betaling.service.GroupService
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class GroupControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private GroupController groupController
    private GroupService groupService

    void setup() {
        groupService = Mock(GroupService)
        groupController = new GroupController(groupService: groupService)
        mockMvc = standaloneSetup(groupController)
    }

    def "Get all customer groups"() {
        given:
        def customer = new Kunde(navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'))

        when:
        def response = mockMvc.perform(get('/api/group'))

        then:
        1 * groupService.getAllCustomerGroups() >> [new KundeGruppe(navn: 'testgruppe', beskrivelse: 'test', kundeliste: [customer])]

        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testgruppe'))
                .andExpect(jsonPathEquals('$[0].kundeliste[0].navn.etternavn','Testesen'))
    }
}
