package no.fint.betaling.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.fintdata.InvoiceIssuerRepository
import no.fint.betaling.fintdata.LineItemRepository
import no.fint.betaling.fintdata.OrganisationRepository
import no.fint.betaling.fintdata.TaxCodeRepository
import no.fint.betaling.invoiceissuer.InvoiceIssuerService
import no.fint.betaling.model.Organisation
import no.fint.betaling.organisation.OrganisationService
import no.fint.betaling.user.UserRepository
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources
import no.novari.fint.model.resource.okonomi.faktura.FakturautstederResource
import no.novari.fint.model.resource.okonomi.faktura.FakturautstederResources
import no.novari.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResource
import no.novari.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResources
import no.novari.fint.model.resource.okonomi.kodeverk.VareResource
import no.novari.fint.model.resource.okonomi.kodeverk.VareResources
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class InvoiceIssuerFixtureSpec extends Specification {
    private ObjectMapper mapper = new ObjectMapper()
    private RestUtil restUtil = Mock()
    private Endpoints endpoints = new Endpoints()
    private TaxCodeRepository taxes = new TaxCodeRepository(restUtil, endpoints)
    private LineItemRepository items = new LineItemRepository(endpoints, restUtil, taxes)
    private OrganisationRepository organisations = new OrganisationRepository(restUtil, endpoints)
    private InvoiceIssuerRepository issuers = new InvoiceIssuerRepository(restUtil, endpoints, items, organisations)
    private boolean itemsAvailable = true

    def setup() {
        restUtil.getWithRetry(_, _) >> { Class type, String uri ->
            def resources = type.getDeclaredConstructor().newInstance()
            if (type == FakturautstederResources) {
                resources.addResource(fixture('fakturautsteder.json', FakturautstederResource))
            } else if (type == OrganisasjonselementResources) {
                resources.addResource(fixture('organisasjon_skole.json', OrganisasjonselementResource))
                resources.addResource(fixture('organisasjon_topp.json', OrganisasjonselementResource))
            } else if (type == MerverdiavgiftResources) {
                resources.addResource(fixture('merverdiavgift.json', MerverdiavgiftResource))
            } else if (type == VareResources && itemsAvailable) {
                ['vare.json', 'vare_002.json', 'vare_003.json', 'vare_004.json'].each {
                    resources.addResource(fixture(it, VareResource))
                }
            }
            Mono.fromCallable { resources }.subscribeOn(Schedulers.parallel())
        }
    }

    def "invoice issuer fixture resolves all four numeric items and VAT"() {
        when:
        def principal = issuers.getInvoiceIssuers().first()

        then:
        principal.code == '20'
        principal.organisation.organisationNumber == '20'
        principal.lineitems*.itemCode.toSet() == ['1', '2', '3', '4'].toSet()
        principal.lineitems.collectEntries { [(it.itemCode): it.itemPrice] } == ['1': 250L, '2': 150L, '3': 500L, '4': 300L]
        principal.lineitems.every { it.taxrate == 0L }
    }

    def "an issuer initially fetched before items are available gains them after item cache refresh"() {
        given:
        itemsAvailable = false
        assert issuers.getInvoiceIssuers().first().lineitems.empty

        when:
        itemsAvailable = true
        items.update()

        then:
        issuers.getInvoiceIssuers().first().lineitems*.itemCode.toSet() == ['1', '2', '3', '4'].toSet()
    }

    def "invoice issuer with cold dependencies can be fetched from a non-blocking thread"() {
        given:
        def organisationService = Mock(OrganisationService)
        organisationService.getOrganisationByOrganisationNumber('20') >>
                Mono.just(new Organisation(organisationNumber: '20', name: 'Test videregående skole'))
        def service = new InvoiceIssuerService(organisationService, issuers, Mock(UserRepository))
        service.principalMatchingStrategy = 'default'

        expect:
        StepVerifier.create(service.getInvoiceIssuer('20').subscribeOn(Schedulers.parallel()))
                .assertNext { principal ->
                    assert principal.lineitems*.itemCode.toSet() == ['1', '2', '3', '4'].toSet()
                }
                .expectComplete()
                .verify(Duration.ofSeconds(5))
    }

    private <T> T fixture(String filename, Class<T> type) {
        mapper.readValue(getClass().getResource('/fint_betaling_ressurser/' + filename), type)
    }
}
