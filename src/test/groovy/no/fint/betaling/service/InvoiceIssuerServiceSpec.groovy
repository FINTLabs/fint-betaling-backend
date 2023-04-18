package no.fint.betaling.service

import no.fint.betaling.exception.PrincipalNotFoundException
import no.fint.betaling.model.Organisation
import no.fint.betaling.model.Principal
import no.fint.betaling.repository.InvoiceIssuerRepository
import no.fint.betaling.repository.UserRepository
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Unroll

class InvoiceIssuerServiceSpec extends Specification {

    InvoiceIssuerService invoiceIssuerService
    OrganisationService organisationService = Mock()
    InvoiceIssuerRepository invoiceIssuerRepository = Mock()
    UserRepository userRepository = Mock()

    def setup() {
        invoiceIssuerService = new InvoiceIssuerService(organisationService, invoiceIssuerRepository, userRepository)
    }

    @Unroll
    def "getInvoiceIssuer with principalMatchingStrategy = #strategy returns correct principal"() {
        given:
        def organizationNumber = "123456789"
        def organisation = new Organisation(organisationNumber: organizationNumber, name: "Test Organisation")
        def principal = new Principal(description: principalDescription, organisation: organisation)
        invoiceIssuerService.principalMatchingStrategy = strategy
        organisationService.getOrganisationByOrganisationNumber(organizationNumber) >> Mono.just(organisation)
        invoiceIssuerRepository.getInvoiceIssuers() >> [principal]

        when:
        def result = invoiceIssuerService.getInvoiceIssuer(organizationNumber).block()

        then:
        result == principal

        where:
        strategy          | principalDescription
        "byOrgnummer"     | "123456789"
        "default"         | "Test Organisation"
    }

    def "getInvoiceIssuer byOrgnummer throws PrincipalNotFoundException"() {
        given:
        def organizationNumber = "123456789"
        def organisation = new Organisation(organisationNumber: organizationNumber, name: "Test Organisation")
        def principal = new Principal(description: "Another Organisation", organisation: organisation)
        invoiceIssuerService.principalMatchingStrategy = "byOrgnummer"
        organisationService.getOrganisationByOrganisationNumber(_) >> Mono.empty()
        invoiceIssuerRepository.getInvoiceIssuers() >> [principal]

        when:
        invoiceIssuerService.getInvoiceIssuer("987654321").block()

        then:
        thrown(PrincipalNotFoundException)
    }

    def "getInvoiceIssuer byName throws PrincipalNotFoundException"() {
        given:
        def organizationNumber = "123456789"
        def organisation = new Organisation(organisationNumber: organizationNumber, name: "Test Organisation")
        def principal = new Principal(description: "Another Organisation", organisation: organisation)
        invoiceIssuerService.principalMatchingStrategy = "default"
        organisationService.getOrganisationByOrganisationNumber(organizationNumber) >> Mono.just(organisation)
        invoiceIssuerRepository.getInvoiceIssuers() >> [principal]

        when:
        invoiceIssuerService.getInvoiceIssuer(organizationNumber).block()

        then:
        thrown(PrincipalNotFoundException)
    }
}
