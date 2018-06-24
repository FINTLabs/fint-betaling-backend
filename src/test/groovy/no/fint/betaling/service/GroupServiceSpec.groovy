package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.FintLinks
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.*
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private RestService restService
    private KundeFactory kundeFactory
    private GroupService groupService

    void setup() {
        def elevforholdResource = new ElevforholdResource()
        elevforholdResource.addElev(Link.with('link.to.ElevResource'))
        def medlemskapResource = new MedlemskapResource()
        medlemskapResource.addMedlem(Link.with('link.to.ElevforholdResource'))

        def kunde = new Kunde(navn: new Personnavn(fornavn: 'Test', etternavn: 'Testesen'))

        restService = Mock(RestService) {
            getResource(ElevforholdResource, _ as String, _ as String) >> elevforholdResource
            getResource(ElevResource, _ as String, _ as String) >> new ElevResource()
            getResource(MedlemskapResource, _ as String, _ as String) >> medlemskapResource
        }
        kundeFactory = Mock(KundeFactory) {
            getKunde(_ as String, _ as FintLinks) >> kunde
        }
        groupService = new GroupService(restService: restService, kundeFactory: kundeFactory)
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
        customerList.get(0).getKundeliste().get(0).navn.fornavn == 'Test'
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
        customerList.get(0).getKundeliste().get(0).navn.fornavn == 'Test'
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
        customerList.get(0).getKundeliste().get(0).navn.fornavn == 'Test'
    }

    def populateResource(def resource) {
        resource.beskrivelse = 'test resource'
        resource.navn = 'testgruppe'
        resource.periode = new ArrayList<>()
        def identifikator = new Identifikator()
        identifikator.setIdentifikatorverdi('test')
        resource.systemId = identifikator
        resource.addMedlemskap(Link.with('link.to.MedlemskapResource'))
        return resource
    }
}
