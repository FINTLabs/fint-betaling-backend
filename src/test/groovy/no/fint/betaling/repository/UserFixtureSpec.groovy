package no.fint.betaling.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.fintdata.OrganisationRepository
import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.fintdata.SchoolResourceRepository
import no.fint.betaling.user.UserRepository
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.fint.model.resource.utdanning.elev.SkoleressursResource
import no.novari.fint.model.resource.utdanning.utdanningsprogram.SkoleResource
import no.novari.fint.model.resource.utdanning.utdanningsprogram.SkoleResources
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

class UserFixtureSpec extends Specification {
    private ObjectMapper mapper = new ObjectMapper()

    @Unroll
    def "fixture employee has a school and owning organisation with warm cache = #warmCache"() {
        given:
        def endpoints = new Endpoints(
                employee: '/administrasjon/personal/personalressurs',
                school: '/utdanning/utdanningsprogram/skole',
                organisationselement: '/administrasjon/organisasjon/organisasjonselement',
                schoolResource: '/utdanning/elev/skoleressurs')
        def restUtil = Mock(RestUtil)
        def schoolRepository = new SchoolRepository(restUtil, endpoints)
        def organisationRepository = new OrganisationRepository(restUtil, endpoints)
        def schoolResourceRepository = new SchoolResourceRepository(restUtil, endpoints)
        def client = new FintClient(restUtil, endpoints, schoolRepository, schoolResourceRepository)
        def users = new UserRepository(organisationRepository, client, schoolRepository)
        restUtil.get(_, _) >> { Class type, String uri ->
            Mono.fromCallable {
                if (type == PersonalressursResource) return fixture('personalressurs.json', type)
                if (type == PersonResource) return fixture('person.json', type)
                if (type == SkoleressursResource) return fixture('skoleressurs.json', type)
                throw new IllegalArgumentException("Unexpected single-resource fetch: $type")
            }.subscribeOn(Schedulers.parallel())
        }
        restUtil.getWithRetry(_, _) >> { Class type, String uri ->
            Mono.fromCallable {
                if (type == SkoleResources) {
                    def resources = new SkoleResources()
                    resources.addResource(fixture('skole.json', SkoleResource))
                    return resources
                }
                if (type == OrganisasjonselementResources) {
                    def resources = new OrganisasjonselementResources()
                    resources.addResource(fixture('organisasjon_skole.json', OrganisasjonselementResource))
                    resources.addResource(fixture('organisasjon_topp.json', OrganisasjonselementResource))
                    return resources
                }
                throw new IllegalArgumentException("Unexpected collection fetch: $type")
            }.subscribeOn(Schedulers.parallel())
        }
        if (warmCache) schoolRepository.update()

        expect:
        StepVerifier.create(users.mapUserFromResources('132323232323', false)
                .subscribeOn(Schedulers.parallel()))
                .assertNext { user ->
                    assert user.name == 'Sander Testansatt'
                    assert user.organisationUnits*.organisationNumber == ['20']
                    assert user.organisationUnits*.name == ['Test videregående skole']
                    assert user.organisation.organisationNumber == '999999999'
                    assert user.organisation.name == 'Test fylkeskommune'
                }
                .expectComplete()
                .verify(Duration.ofSeconds(5))

        where:
        warmCache << [false, true]
    }

    def "school and organisation fixtures contain their required primary identifiers"() {
        expect:
        fixture('skole.json', SkoleResource).systemId?.identifikatorverdi
        ['organisasjon_skole.json', 'organisasjon_topp.json'].every { filename ->
            def organisation = fixture(filename, OrganisasjonselementResource)
            organisation.organisasjonsId?.identifikatorverdi && organisation.organisasjonsKode?.identifikatorverdi
        }
    }

    private <T> T fixture(String filename, Class<T> type) {
        mapper.readValue(getClass().getResource('/fint_betaling_ressurser/' + filename), type)
    }
}
