package no.fint.betaling.repository

import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.okonomi.MvakodeResource
import no.fint.model.resource.administrasjon.okonomi.MvakodeResources
import spock.lang.Specification

class TaxcodeRepositorySpec extends Specification {

    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/mvakode'
    def repository = new TaxcodeRepository(restUtil: restUtil, taxcodeEndpoint: endpoint)

    def 'Fetching tax codes should update first'() {
        given:
        def mvaCodes = new MvakodeResources()
        def mvaCode = new MvakodeResource(kode: '25', navn: '25%', promille: 250, systemId: new Identifikator(identifikatorverdi: 'test'))
        mvaCode.addSelf(Link.with('http://mvacode'))
        mvaCodes.addResource(mvaCode)

        when:
        def result = repository.getTaxcodes()

        then:
        result.size() == 1
        1 * restUtil.getUpdates(MvakodeResources, endpoint) >> mvaCodes
    }
}
