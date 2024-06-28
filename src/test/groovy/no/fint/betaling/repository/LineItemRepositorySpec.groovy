package no.fint.betaling.repository

import no.fint.betaling.fintdata.LineItemRepository
import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.fintdata.TaxCodeRepository
import no.fint.betaling.model.Taxcode
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.kodeverk.VareResource
import no.fint.model.resource.okonomi.kodeverk.VareResources
import reactor.core.publisher.Mono
import spock.lang.Specification

class LineItemRepositorySpec extends Specification {

    def taxcodeRepository = Mock(TaxCodeRepository)
    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/varelinje'
    def endpoints = Mock(Endpoints) {
        getVare() >> endpoint
    }
    def repository = new LineItemRepository(endpoints, restUtil, taxcodeRepository)

    def 'Update line items'() {
        given:
        def varelinjeResource = new VareResource(
                navn: 'testOrder',
                enhet: 'unit',
                pris: 1000,
                kode: 'code',
                systemId: new Identifikator(identifikatorverdi: 'test'))
        varelinjeResource.addSelf(Link.with('http://varelinje'))
        varelinjeResource.addMerverdiavgift(Link.with('http://mvakode/1234'))
        def resources = new VareResources()
        resources.addResource(varelinjeResource)

        when:
        repository.update()

        then:
        1 * restUtil.getWithRetry(_, _) >> Mono.just(resources)
        1 * taxcodeRepository.getTaxcodeByUri('http://mvakode/1234') >> new Taxcode(rate: 0.25)
    }

    def 'Fetching lime items should update first'() {
        given:
        def varelinjeResource = new VareResource(
                navn: 'testOrder',
                enhet: 'unit',
                pris: 1000,
                kode: 'code',
                systemId: new Identifikator(identifikatorverdi: 'test'))
        varelinjeResource.addSelf(Link.with('http://varelinje'))
        varelinjeResource.addMerverdiavgift(Link.with('http://mvakode/2345'))
        def resources = new VareResources()
        resources.addResource(varelinjeResource)

        when:
        def result = repository.getLineItems()

        then:
        result.size() == 1
        1 * restUtil.getWithRetry(_, _) >> Mono.just(resources)
        1 * taxcodeRepository.getTaxcodeByUri(_ as String) >> new Taxcode(rate: 0.25)

    }
}
