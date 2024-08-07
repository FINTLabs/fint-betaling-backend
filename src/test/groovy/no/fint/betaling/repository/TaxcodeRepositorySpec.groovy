package no.fint.betaling.repository

import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.fintdata.TaxCodeRepository
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResource
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResources
import reactor.core.publisher.Mono
import spock.lang.Specification

class TaxcodeRepositorySpec extends Specification {

    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/mvakode'
    def endpoints = Mock(Endpoints) {
        getTaxCode() >> endpoint
    }
    def repository = new TaxCodeRepository(restUtil, endpoints)

    def 'Fetching tax codes should update first'() {
        given:
        def mvaCodes = new MerverdiavgiftResources()
        def mvaCode = new MerverdiavgiftResource(kode: '25', navn: '25%', sats: 250, systemId: new Identifikator(identifikatorverdi: 'test'))
        mvaCode.addSelf(Link.with('http://mvacode'))
        mvaCodes.addResource(mvaCode)

        when:
        def result = repository.getTaxcodes()

        then:
        result.size() == 1
        1 * restUtil.getWithRetry(MerverdiavgiftResources, endpoint) >> Mono.just(mvaCodes)
    }
}
