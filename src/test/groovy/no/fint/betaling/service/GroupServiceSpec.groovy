package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.PersonResources
import no.fint.model.resource.utdanning.elev.*
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private RestService restService
    private GroupService groupService
    private KundeFactory kundeFactory

    void setup() {
        def medlemskapResource = new MedlemskapResource(systemId: new Identifikator(identifikatorverdi: 'test'))
        medlemskapResource.addMedlem(Link.with('link.to.ElevforholdResource'))
        medlemskapResource.addLink('self', Link.with('link.to.MedlemskapResource'))
        def medlemskapResources = new MedlemskapResources()
        medlemskapResources.addResource(medlemskapResource)

        def elevforholdResource = new ElevforholdResource(
                systemId: new Identifikator(identifikatorverdi: 'test'),
                beskrivelse: 'student relation')
        elevforholdResource.addElev(Link.with('link.to.ElevResource'))
        elevforholdResource.addLink('self', Link.with('link.to.ElevforholdResource'))
        elevforholdResource.addMedlemskap(Link.with('link.to.MedlemskapResource'))
        def elevforholdResources = new ElevforholdResources()
        elevforholdResources.addResource(elevforholdResource)

        def elevResource = new ElevResource(systemId: new Identifikator(identifikatorverdi: 'test'))
        elevResource.addLink('person', Link.with('link.to.PersonResource'))
        elevResource.addLink('self', Link.with('link.to.ElevResource'))
        elevResource.addElevforhold(Link.with('link.to.ElevforholdResource'))
        def elevResources = new ElevResources()
        elevResources.addResource(elevResource)

        def personResource = new PersonResource(
                fodselsnummer: new Identifikator(identifikatorverdi: '12345678901'),
                navn: new Personnavn(fornavn: 'Test', etternavn: 'Testesen'))
        personResource.addLink('self', Link.with('link.to.PersonResource'))
        personResource.addElev(Link.with('link.to.ElevResource'))
        def personResources = new PersonResources()
        personResources.addResource(personResource)

        def kunde = new Kunde(navn: new Personnavn(fornavn: 'Test', etternavn: 'Testesen'))

        restService = Mock(RestService) {
            getResource(ElevforholdResources, _ as String, _ as String) >> elevforholdResources
            getResource(ElevResources, _ as String, _ as String) >> elevResources
            getResource(MedlemskapResources, _ as String, _ as String) >> medlemskapResources
            getResource(PersonResources, _ as String, _ as String) >> personResources
        }
        kundeFactory = Mock(KundeFactory) {
            getKunde(_ as PersonResource) >> kunde
        }
        groupService = new GroupService(
                restService: restService,
                kundeFactory: kundeFactory,
                basisgruppeEndpoint: "endpoints/basisgruppe",
                undervisningsgruppeEndpoint: "endpoints/undervisningsgruppe",
                kontaktlarergruppeEndpoint: "endpoints/kontaktlarergruppe",
                personEndpoint: "endpoints/person",
                studentEndpoint: "endpoints/elev",
                membershipEndpoint: "endpoints/medlemskap",
                studentRelationEndpoint: "endpoints/elevforhold"
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
