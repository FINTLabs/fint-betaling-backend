package no.fint.betaling.repository

import no.fint.betaling.model.Taxcode
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.faktura.VareResource
import no.fint.model.resource.administrasjon.faktura.VareResources
import spock.lang.Specification

class LineitemRepositorySpec extends Specification {

    def taxcodeRepository = Mock(TaxcodeRepository)
    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/varelinje'
    def repository = new LineitemRepository(restUtil: restUtil, lineitemEndpoint: endpoint, taxcodeRepository: taxcodeRepository)

    def 'Update line items'() {
        given:
        def vareResource = new VareResource(
                navn: 'testOrder',
                enhet: 'unit',
                pris: 1000,
                kode: 'code',
                systemId: new Identifikator(identifikatorverdi: 'test'))
        vareResource.addSelf(Link.with('http://varelinje'))
        vareResource.addMerverdiavgift(Link.with('http://mvakode/1234'))
        def resources = new VareResources()
        resources.addResource(vareResource)


        when:
        repository.updateLineitems()

        then:
        1 * restUtil.getUpdates(_ as Class<VareResources>, _ as String) >> resources
        1 * taxcodeRepository.getTaxcodeByCode('1234') >> new Taxcode(rate: 0.25)
    }

    def 'Fetching lime items should update first'() {
        given:
        def vareResource = new VareResource(
                navn: 'testOrder',
                enhet: 'unit',
                pris: 1000,
                kode: 'code',
                systemId: new Identifikator(identifikatorverdi: 'test'))
        vareResource.addSelf(Link.with('http://varelinje'))
        vareResource.addMerverdiavgift(Link.with('http://mvakode/2345'))
        def resources = new VareResources()
        resources.addResource(vareResource)

        when:
        def result = repository.getLineitems()

        then:
        result.size() == 1
        1 * restUtil.getUpdates(_ as Class<VareResources>, _ as String) >> resources
        1 * taxcodeRepository.getTaxcodeByCode('2345') >> new Taxcode(rate: 0.25)

    }
}
