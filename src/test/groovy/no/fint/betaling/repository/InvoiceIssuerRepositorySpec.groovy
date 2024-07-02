package no.fint.betaling.repository

import no.fint.betaling.fintdata.InvoiceIssuerRepository
import no.fint.betaling.fintdata.LineItemRepository
import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.model.Lineitem
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.organisation.OrganisationRepository
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.okonomi.faktura.FakturautstederResource
import no.fint.model.resource.okonomi.faktura.FakturautstederResources
import reactor.core.publisher.Mono
import spock.lang.Specification

class InvoiceIssuerRepositorySpec extends Specification {

    def restUtil = Mock(RestUtil)
    def endpoint = 'http://localhost/oppdragsgiver'
    def endpoints = Mock(Endpoints) {
        getInvoiceIssuer() >> endpoint
    }
    def lineitemRepository = Mock(LineItemRepository)
    def organisationRepository = Mock(OrganisationRepository)

    def repository = new InvoiceIssuerRepository(
            restUtil,
            endpoints,
            lineitemRepository,
            organisationRepository
    )

//    def 'Fetching principals should update first'() {
//        given:
//        def fakturautstederResources = new FakturautstederResources()
//        def fakturautstederResource = new FakturautstederResource(
//                navn: 'test',
//                systemId: new Identifikator(identifikatorverdi: 'test')
//        )
//        fakturautstederResource.addSelf(Link.with('http://oppdragsgiver'))
//        fakturautstederResource.addVare(Link.with('http://varelinje'))
//        fakturautstederResource.addOrganisasjonselement(Link.with('http://organisasjonselement'))
//        fakturautstederResources.addResource(fakturautstederResource)
//
//        when:
//        def result = repository.getInvoiceIssuers()
//
//        then:
//        1 * restUtil.getUpdates(FakturautstederResources, endpoint) >> Mono.just(fakturautstederResources)
//        1 * lineitemRepository.getLineItemByUri('http://varelinje') >> new Lineitem(itemCode: 'abc')
//        result.size() == 1
//    }

    def 'Fetching line item by URI'() {
        given:
        String uri = 'http://varelinje'

        when:
        def result = lineitemRepository.getLineItemByUri(uri)

        then:
        1 * lineitemRepository.getLineItemByUri(uri) >> new Lineitem(itemCode: 'abc')
        result.itemCode == 'abc'
    }
}
