package no.fint.betaling.util

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.common.exception.SkoleressursException
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.fintdata.SchoolResourceRepository
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.utdanning.elev.SkoleressursResource
import no.novari.fint.model.resource.utdanning.elev.SkoleressursResources
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class FintClientSpec extends Specification {
    private RestUtil restUtil = Mock()
    private SchoolRepository schoolRepository = Mock()
    private SchoolResourceRepository schoolResourceRepository = Mock()
    private Endpoints endpoints = new Endpoints(schoolResource: '/utdanning/elev/skoleressurs')
    private FintClient client = new FintClient(restUtil, endpoints, schoolRepository, schoolResourceRepository)

    def "employee fixture resolves its direct school resource link without a populated cache"() {
        given:
        def employee = new ObjectMapper().readValue(
                getClass().getResource('/fint_betaling_ressurser/personalressurs.json'),
                PersonalressursResource)
        def schoolResource = new SkoleressursResource()
        def href = employee.skoleressurs.first().href

        when:
        def result = client.getSkoleressurs(employee).block(Duration.ofSeconds(5))

        then:
        result.is(schoolResource)
        1 * restUtil.get(SkoleressursResource, href) >> Mono.just(schoolResource)
        0 * schoolResourceRepository._
    }

    def "reverse relation is loaded from a cold cache without blocking the event loop"() {
        given:
        def employee = new PersonalressursResource()
        def employeeLink = Link.with('https://example.com/administrasjon/personal/personalressurs/systemid/employee-1')
        employee.addSelf(employeeLink)
        def schoolResource = new SkoleressursResource()
        schoolResource.addPersonalressurs(employeeLink)
        def resources = new SkoleressursResources()
        resources.addResource(schoolResource)
        def repository = new SchoolResourceRepository(restUtil, endpoints)
        client = new FintClient(restUtil, endpoints, schoolRepository, repository)
        restUtil.getWithRetry(SkoleressursResources, endpoints.schoolResource) >>
                Mono.fromCallable { resources }.subscribeOn(Schedulers.parallel())

        expect:
        StepVerifier.create(client.getSkoleressurs(employee).subscribeOn(Schedulers.parallel()))
                .expectNext(schoolResource)
                .expectComplete()
                .verify(Duration.ofSeconds(5))
    }

    def "missing direct and reverse relationships produce a reactive bad request"() {
        given:
        def employee = new PersonalressursResource()
        employee.addSelf(Link.with('https://example.com/personalressurs/systemid/missing'))
        schoolResourceRepository.isEmpty() >> false
        schoolResourceRepository.getResourceByLink(_) >> Optional.empty()

        expect:
        StepVerifier.create(client.getSkoleressurs(employee))
                .expectErrorSatisfies { error ->
                    assert error instanceof SkoleressursException
                    assert error.statusCode.value() == 400
                    assert error.reason == 'Personalressursen har ingen relasjon til en skoleressurs'
                }
                .verify(Duration.ofSeconds(5))
    }

    def "a direct link HTTP failure retains its cause instead of reporting a missing relationship"() {
        given:
        def employee = new PersonalressursResource()
        def href = 'https://example.com/utdanning/elev/skoleressurs/systemid/SR-001'
        employee.addSkoleressurs(Link.with(href))
        def failure = WebClientResponseException.create(404, 'Not Found', HttpHeaders.EMPTY, new byte[0], null)

        when:
        StepVerifier.create(client.getSkoleressurs(employee))
                .expectErrorMatches { it.is(failure) }
                .verify(Duration.ofSeconds(5))

        then:
        1 * restUtil.get(SkoleressursResource, href) >> Mono.error(failure)
        0 * schoolResourceRepository._
    }
}
