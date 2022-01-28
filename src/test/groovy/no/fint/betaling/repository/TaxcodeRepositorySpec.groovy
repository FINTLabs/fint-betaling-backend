package no.fint.betaling.repository

import no.fint.betaling.util.FintEndpointsRepository
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResource
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResources
import spock.lang.Specification

class TaxcodeRepositorySpec extends Specification {

    def restUtil = Mock(FintEndpointsRepository)
    def endpoint = 'http://localhost/mvakode'
    def repository = new TaxcodeRepository(fintEndpointsRepository: restUtil, taxcodeEndpoint: endpoint)

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
        1 * restUtil.getUpdates(MerverdiavgiftResources, endpoint) >> mvaCodes
    }
}
