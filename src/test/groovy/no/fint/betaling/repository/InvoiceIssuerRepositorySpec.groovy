package no.fint.betaling.repository

import no.fint.betaling.config.Endpoints
import no.fint.betaling.model.Lineitem
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.faktura.FakturautstederResource
import no.fint.model.resource.okonomi.faktura.FakturautstederResources
import spock.lang.Specification

class InvoiceIssuerRepositorySpec extends Specification {

    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/oppdragsgiver'
    def endpoints = Mock(Endpoints) {
        getInvoiceIssuer() >> endpoint
    }
    def lineitemRepository = Mock(LineitemRepository)
    def organisationRepository = Mock(OrganisationRepository)

    def repository = new InvoiceIssuerRepository(
            restUtil,
            lineitemRepository,
            organisationRepository,
            endpoints
    )

    def 'Fetching principals should update first'() {
        given:
        def fakturautstederResources = new FakturautstederResources()
        def fakturautstederResource = new FakturautstederResource(
                navn: 'test',
                systemId: new Identifikator(identifikatorverdi: 'test')
        )
        fakturautstederResource.addSelf(Link.with('http://oppdragsgiver'))
        fakturautstederResource.addVare(Link.with('http://varelinje'))
        fakturautstederResource.addOrganisasjonselement(Link.with('http://organisasjonselement'))
        fakturautstederResources.addResource(fakturautstederResource)

        when:
        def result = repository.getInvoiceIssuers()

        then:
        1 * restUtil.getUpdates(FakturautstederResources, endpoint) >> fakturautstederResources
        1 * lineitemRepository.getLineitemByUri(_ as String) >> new Lineitem(itemCode: 'abc')
        result.size() == 1
    }
}
