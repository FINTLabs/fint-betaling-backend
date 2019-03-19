package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.BasisgruppeResource
import no.fint.model.resource.utdanning.elev.BasisgruppeResources
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResource
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResources
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private CacheService cacheService
    private GroupService groupService
    private CustomerService customerService
    private MembershipService membershipService
    private StudentRelationService studentRelationService

    void setup() {
        def kunde = new Kunde(
                navn: new Personnavn(etternavn: 'Testesen', fornavn: 'Test'),
                person: Link.with('link.to.PersonResource'),
                elev: Link.with('link.to.ElevResource'),
                kundenummer: '12345678901')

        membershipService = Mock(MembershipService) {
            getMemberships(_ as String) >> []
        }

        customerService = Mock(CustomerService) {
            getCustomers(_ as String, _) >> [kunde]
        }

        studentRelationService = Mock(StudentRelationService) {
            getStudentRelationships(_) >> [(Link.with('link.to.ElevforholdResource')): Link.with('link.to.ElevResource')]
        }

        cacheService = Mock()
        groupService = new GroupService(
                cacheService: cacheService,
                membershipService: membershipService,
                customerService: customerService,
                studentRelationService: studentRelationService,
                basisgruppeEndpoint: "endpoints/basisgruppe",
                undervisningsgruppeEndpoint: "endpoints/undervisningsgruppe",
                kontaktlarergruppeEndpoint: "endpoints/kontaktlarergruppe"
        )
        groupService.init()
    }


    def "Get customer groups from kontaktlarergruppe"() {
        given:
        def resources = new KontaktlarergruppeResources()
        resources.addResource(populateResource(new KontaktlarergruppeResource()) as KontaktlarergruppeResource)

        when:
        def customerList = groupService.getCustomerGroupListFromKontaktlarergruppe('rogfk.no')

        then:
        1 * cacheService.getUpdates(KontaktlarergruppeResources, _ as String, _ as String) >> resources

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0).kundenummer == '12345678901'
    }

    def "Get customer groups from undervisningsgruppe"() {
        given:
        def resources = new UndervisningsgruppeResources()
        resources.addResource(populateResource(new UndervisningsgruppeResource()) as UndervisningsgruppeResource)

        when:
        def customerList = groupService.getCustomerGroupListFromUndervisningsgruppe('rogfk.no')

        then:
        1 * cacheService.getUpdates(UndervisningsgruppeResources, _ as String, _ as String) >> resources

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0).kundenummer == '12345678901'
    }

    def "Get customer groups from basisgruppe"() {
        given:
        def resources = new BasisgruppeResources()
        resources.addResource(populateResource(new BasisgruppeResource()) as BasisgruppeResource)

        when:
        def customerList = groupService.getCustomerGroupListFromBasisgruppe('rogfk.no')

        then:
        1 * cacheService.getUpdates(BasisgruppeResources, _ as String, _ as String) >> resources

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0).kundenummer == '12345678901'
    }

    def populateResource(def resource) {
        resource.beskrivelse = 'test resource'
        resource.navn = 'testgruppe'
        resource.periode = new ArrayList<>()
        def identifikator = new Identifikator()
        identifikator.setIdentifikatorverdi('test')
        resource.systemId = identifikator
        resource.addElevforhold(Link.with('link.to.ElevforholdResource'))
        resource.addLink("self", Link.with('link.to.Gruppe'))
        return resource
    }
}
