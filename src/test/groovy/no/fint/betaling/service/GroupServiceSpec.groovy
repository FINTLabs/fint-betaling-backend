package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.utdanning.elev.*
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private RestService restService
    private GroupService groupService
    private KundeFactory kundeFactory
    private CustomerService customerService
    private MembershipService membershipService
    private StudentRelationService studentRelationService

    void setup() {
        def medlemskapResource = new MedlemskapResource(systemId: new Identifikator(identifikatorverdi: 'test'))
        medlemskapResource.addGruppe(Link.with('link.to.Gruppe'))
        medlemskapResource.addMedlem(Link.with('link.to.ElevforholdResource'))
        medlemskapResource.addLink('self', Link.with('link.to.MedlemskapResource'))

        def kunde = new Kunde(
                navn: 'Testesen, Test',
                person: Link.with('link.to.PersonResource'),
                elev: Link.with('link.to.ElevResource'),
                setKundeid: '12345678901')

        membershipService = Mock(MembershipService) {
            getMemberships(_ as String) >> [medlemskapResource]
        }

        customerService = Mock(CustomerService) {
            getCustomers(_ as String, _) >> [kunde]
        }

        studentRelationService = Mock(StudentRelationService) {
            getStudentRelationships(_) >> [(Link.with('link.to.ElevforholdResource')): Link.with('link.to.ElevResource')]
        }

        restService = Mock(RestService)
        kundeFactory = Mock(KundeFactory) {
            getKunde(_ as PersonResource) >> kunde
        }
        groupService = new GroupService(
                restService: restService,
                kundeFactory: kundeFactory,
                membershipService: membershipService,
                customerService: customerService,
                studentRelationService: studentRelationService,
                basisgruppeEndpoint: "endpoints/basisgruppe",
                undervisningsgruppeEndpoint: "endpoints/undervisningsgruppe",
                kontaktlarergruppeEndpoint: "endpoints/kontaktlarergruppe"
        )
    }


    def "Get customer groups from kontaktlarergruppe"() {
        given:
        def resources = new KontaktlarergruppeResources()
        resources.addResource(populateResource(new KontaktlarergruppeResource()) as KontaktlarergruppeResource)

        when:
        def customerList = groupService.getCustomerGroupListFromKontaktlarergruppe('rogfk.no')

        then:
        1 * restService.getResource(KontaktlarergruppeResources, _ as String, _ as String) >> resources

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0) == '12345678901'
    }

    def "Get customer groups from undervisningsgruppe"() {
        given:
        def resources = new UndervisningsgruppeResources()
        resources.addResource(populateResource(new UndervisningsgruppeResource()) as UndervisningsgruppeResource)

        when:
        def customerList = groupService.getCustomerGroupListFromUndervisningsgruppe('rogfk.no')

        then:
        1 * restService.getResource(UndervisningsgruppeResources, _ as String, _ as String) >> resources

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0) == '12345678901'
    }

    def "Get customer groups from basisgruppe"() {
        given:
        def resources = new BasisgruppeResources()
        resources.addResource(populateResource(new BasisgruppeResource()) as BasisgruppeResource)

        when:
        def customerList = groupService.getCustomerGroupListFromBasisgruppe('rogfk.no')

        then:
        1 * restService.getResource(BasisgruppeResources, _ as String, _ as String) >> resources

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0) == '12345678901'
    }

    def populateResource(def resource) {
        resource.beskrivelse = 'test resource'
        resource.navn = 'testgruppe'
        resource.periode = new ArrayList<>()
        def identifikator = new Identifikator()
        identifikator.setIdentifikatorverdi('test')
        resource.systemId = identifikator
        resource.addMedlemskap(Link.with('link.to.MedlemskapResource'))
        resource.addLink("self", Link.with('link.to.Gruppe'))
        return resource
    }
}
