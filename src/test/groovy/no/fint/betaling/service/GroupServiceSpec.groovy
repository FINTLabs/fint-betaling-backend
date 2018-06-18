package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.*
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private RestService restService
    private KundeFactory kundeFactory
    private GroupService groupService

    void setup() {
        restService = Mock(RestService)
        kundeFactory = Mock(KundeFactory)
        groupService = new GroupService(restService: restService, kundeFactory: kundeFactory)
    }

    def "Get customer groups from basisgruppe returns list"() {
        given:
        def elevforholdResource = new ElevforholdResource()
        elevforholdResource.addElev(Link.with('link.to.ElevResource'))

        def medlemskapResource = new MedlemskapResource()
        medlemskapResource.addMedlem(Link.with('link.to.ElevforholdResource'))

        def basisgruppeResource = createBasisgruppeResource()

        def basisgruppeResources = new BasisgruppeResources()
        basisgruppeResources.addResource(basisgruppeResource)

        def kunde = new Kunde(navn: new Personnavn(fornavn: 'Test', etternavn: 'Testesen'))

        when:
        def customerList = groupService.getCustomerGroupListFromBasisgruppe()

        then:
        1 * restService.getBasisgruppeResources(_) >> basisgruppeResources
        1 * restService.getMedlemskapResource(_, _) >> medlemskapResource
        1 * restService.getElevforholdResource(_, _) >> elevforholdResource
        1 * restService.getElevResource(_, _) >> new ElevResource()
        1 * kundeFactory.getKunde(_, _) >> kunde

        customerList.size() == 1
        customerList.get(0).getNavn() == 'testgruppe'
        customerList.get(0).getKundeliste().get(0).navn.fornavn == 'Test'
    }

    private static BasisgruppeResource createBasisgruppeResource() {
        def basisgruppeResource = new BasisgruppeResource()
        basisgruppeResource.beskrivelse = 'test resource'
        basisgruppeResource.navn = 'testgruppe'
        basisgruppeResource.periode = new ArrayList<>()
        def identifikator = new Identifikator()
        identifikator.setIdentifikatorverdi('test')
        basisgruppeResource.systemId = identifikator
        basisgruppeResource.addMedlemskap(Link.with('link.to.MedlemskapResource'))
        return basisgruppeResource
    }
}
