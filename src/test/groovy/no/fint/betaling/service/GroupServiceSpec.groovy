package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.*
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class GroupServiceSpec extends Specification {

    private RestTemplate restTemplate
    private KundeFactory kundeFactory
    private GroupService groupService

    void setup() {
        restTemplate = Mock(RestTemplate)
        kundeFactory = Mock(KundeFactory)
        groupService = new GroupService(restTemplate: restTemplate, kundeFactory: kundeFactory)
    }

    def "Get customer groups returns list"() {
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
        def customerList = groupService.getCustomerGroups()

        then:
        1 * restTemplate.exchange(_, _, _, BasisgruppeResources) >> ResponseEntity.ok(basisgruppeResources)
        1 * restTemplate.exchange(_, _, _, MedlemskapResource) >> ResponseEntity.ok(medlemskapResource)
        1 * restTemplate.exchange(_, _, _, ElevforholdResource) >> ResponseEntity.ok(elevforholdResource)
        1 * restTemplate.exchange(_, _, _, ElevResource) >> ResponseEntity.ok(new ElevResource())
        1 * kundeFactory.getKunde(_) >> kunde

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
