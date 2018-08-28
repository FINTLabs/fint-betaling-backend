package no.fint.betaling.controller


import no.fint.betaling.model.KundeGruppe
import no.fint.betaling.service.GroupService
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class GroupControllerSpec extends MockMvcSpecification {
    private MockMvc mockMvc
    private GroupController groupController
    private GroupService groupService

    void setup() {
        def customer = '12345678901'
        groupService = Mock(GroupService) {
            getAllCustomerGroups(_ as String) >> [new KundeGruppe(navn: 'testgruppe', beskrivelse: 'test', kundeliste: [customer])]
            getCustomerGroupListFromBasisgruppe(_ as String) >> [new KundeGruppe(navn: 'testgruppe', beskrivelse: 'test', kundeliste: [customer])]
            getCustomerGroupListFromKontaktlarergruppe(_ as String) >> [new KundeGruppe(navn: 'testgruppe', beskrivelse: 'test', kundeliste: [customer])]
            getCustomerGroupListFromUndervisningsgruppe(_ as String) >> [new KundeGruppe(navn: 'testgruppe', beskrivelse: 'test', kundeliste: [customer])]
        }
        groupController = new GroupController(groupService: groupService)
        mockMvc = standaloneSetup(groupController)
    }

    def "Get all customer groups"() {
        when:
        def response = mockMvc.perform(get('/api/group'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testgruppe'))
                .andExpect(jsonPathEquals('$[0].kundeliste[0]', '12345678901'))
    }

    def "Get customer groups from basisgruppe"() {
        when:
        def response = mockMvc.perform(get('/api/group/basisgruppe'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testgruppe'))
                .andExpect(jsonPathEquals('$[0].kundeliste[0]', '12345678901'))
    }

    def "Get customer groups from undervisningsgruppe"() {
        when:
        def response = mockMvc.perform(get('/api/group/undervisningsgruppe'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testgruppe'))
                .andExpect(jsonPathEquals('$[0].kundeliste[0]', '12345678901'))
    }

    def "Get customer groups from kontaktlarergruppe"() {
        when:
        def response = mockMvc.perform(get('/api/group/kontaktlarergruppe'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 1))
                .andExpect(jsonPathEquals('$[0].navn', 'testgruppe'))
                .andExpect(jsonPathEquals('$[0].kundeliste[0]', '12345678901'))
    }
}
