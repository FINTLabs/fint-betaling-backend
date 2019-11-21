package no.fint.betaling.repository

import no.fint.betaling.model.Lineitem
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResource
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources
import spock.lang.Specification

class PrincipalRepositorySpec extends Specification {

    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/oppdragsgiver'.toURI()
    def lineitemRepository = Mock(LineitemRepository)
    def repository = new PrincipalRepository(restUtil: restUtil, principalEndpoint: endpoint, lineitemRepository: lineitemRepository)

    def 'Fetching principals should update first'() {
        given:
        def oppdragsgiverResources = new OppdragsgiverResources()
        def oppdragsgiverResource = new OppdragsgiverResource(navn: 'test', systemId: new Identifikator(identifikatorverdi: 'test'))
        oppdragsgiverResource.addSelf(Link.with('http://oppdragsgiver'))
        oppdragsgiverResource.addVarelinje(Link.with('http://varelinje'))
        oppdragsgiverResources.addResource(oppdragsgiverResource)

        when:
        def result = repository.getPrincipals()

        then:
        result.size() == 1
        1 * restUtil.getUpdates(OppdragsgiverResources, endpoint) >> oppdragsgiverResources
        1 * lineitemRepository.getLineitemByUri(_ as URI) >> new Lineitem(code: 'abc')
    }
}
