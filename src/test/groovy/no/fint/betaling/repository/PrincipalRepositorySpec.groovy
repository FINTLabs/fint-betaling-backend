package no.fint.betaling.repository

import no.fint.betaling.model.Lineitem
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.faktura.FakturautstederResource
import no.fint.model.resource.okonomi.faktura.FakturautstederResources
import spock.lang.Specification

class PrincipalRepositorySpec extends Specification {

    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/oppdragsgiver'
    def lineitemRepository = Mock(LineitemRepository)
    def repository = new PrincipalRepository(restUtil: restUtil, principalEndpoint: endpoint, lineitemRepository: lineitemRepository)

    def 'Fetching principals should update first'() {
        given:
        def fakturautstederResources = new FakturautstederResources()
        def fakturautstederResource = new FakturautstederResource(navn: 'test', systemId: new Identifikator(identifikatorverdi: 'test'))
        fakturautstederResource.addSelf(Link.with('http://oppdragsgiver'))
        fakturautstederResource.addVare(Link.with('http://varelinje'))
        fakturautstederResources.addResource(fakturautstederResource)

        when:
        def result = repository.getPrincipals()

        then:
        1 * restUtil.getUpdates(FakturautstederResources , endpoint) >> fakturautstederResources
        1 * lineitemRepository.getLineitemByUri(_ as String) >> new Lineitem(itemCode: 'abc')
        result.size() == 1
    }
}
