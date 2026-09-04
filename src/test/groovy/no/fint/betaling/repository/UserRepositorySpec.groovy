package no.fint.betaling.repository

import no.fint.betaling.common.config.Endpoints
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.fintdata.OrganisationRepository
import no.fint.betaling.fintdata.SchoolRepository
import no.fint.betaling.user.UserRepository
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.felles.kompleksedatatyper.Personnavn
import no.novari.fint.model.resource.Link
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

/**
 * The school and organisation caches fall back to a blocking fetch when cold. Since
 * mapUserFromResources completes on the WebClient event loop, that fetch used to throw
 * "block() ... is not supported in thread reactor-http-epoll-N".
 *
 * Reactor marks Schedulers.parallel() threads as non-blocking in exactly the same way as the
 * event loop, so subscribing there reproduces the failure without a real HTTP server.
 */
class UserRepositorySpec extends Specification {

    private static final String ORGANISATION_HREF = 'link.to.Organisation'

    private RestUtil restUtil
    private FintClient fintClient
    private SchoolRepository schoolRepository
    private OrganisationRepository organisationRepository
    private UserRepository userRepository

    void setup() {
        def endpoints = new Endpoints()
        restUtil = Mock()
        fintClient = Mock()
        schoolRepository = new SchoolRepository(restUtil, endpoints)
        organisationRepository = new OrganisationRepository(restUtil, endpoints)
        userRepository = new UserRepository(organisationRepository, fintClient, schoolRepository)
    }

    def "Admin user is mapped when the school cache is cold on a non-blocking thread"() {
        given:
        def personalressurs = newPersonalressurs()
        fintClient.getPersonalressurs('12345') >> Mono.just(personalressurs)
        fintClient.getPerson(personalressurs) >> Mono.just(newPerson())
        stubResourceFetches()

        expect:
        schoolRepository.isEmpty()

        and:
        StepVerifier
                .create(userRepository.mapUserFromResources('12345', true)
                        .subscribeOn(Schedulers.parallel()))
                .assertNext { user ->
                    assert user.employeeNumber == '12345'
                    assert user.name == 'Ola Nordmann'
                    assert user.organisationUnits*.name == ['HVS']
                    assert user.organisation.name == 'Testfylke'
                }
                .verifyComplete()
    }

    def "Non-admin user is mapped when the organisation cache is cold on a non-blocking thread"() {
        given:
        def personalressurs = newPersonalressurs()
        def school = newSchool()
        fintClient.getPersonalressurs('12345') >> Mono.just(personalressurs)
        fintClient.getPerson(personalressurs) >> Mono.just(newPerson())
        fintClient.getSkoleressurs(personalressurs) >> Mono.just(new SkoleressursResource())
        fintClient.getSkoler(_ as SkoleressursResource) >> [school]
        stubResourceFetches()

        expect:
        organisationRepository.isEmpty()

        and:
        StepVerifier
                .create(userRepository.mapUserFromResources('12345', false)
                        .subscribeOn(Schedulers.parallel()))
                .assertNext { user ->
                    assert user.employeeNumber == '12345'
                    assert user.organisationUnits*.name == ['HVS']
                    assert user.organisation.name == 'Testfylke'
                }
                .verifyComplete()
    }

    /**
     * Endpoints only populates its fields via @Value, so a plain instance leaves every endpoint
     * null and the fetches cannot be told apart by URI. Dispatch on the requested type instead.
     *
     * The Monos must be asynchronous. Mono.just overrides block() to hand back its value without
     * ever building a BlockingSingleSubscriber, so it slips past the non-blocking-thread guard and
     * would hide the very bug this spec pins down. Deferring onto a scheduler mirrors what the real
     * WebClient-backed getWithRetry returns.
     */
    private void stubResourceFetches() {
        restUtil.getWithRetry(_, _) >> { Class clazz, String uri ->
            Mono.fromCallable {
                if (clazz == SkoleResources) return newSchoolResources()
                if (clazz == OrganisasjonselementResources) return newOrganisationResources()
                throw new IllegalArgumentException("Unexpected fetch of ${clazz.simpleName}")
            }.subscribeOn(Schedulers.boundedElastic())
        }
    }

    private static PersonalressursResource newPersonalressurs() {
        def resource = new PersonalressursResource()
        resource.setAnsattnummer(new Identifikator(identifikatorverdi: '12345'))
        resource.addPerson(new Link(verdi: 'link.to.Person'))
        resource.addSelf(new Link(verdi: 'link.to.Personalressurs'))
        return resource
    }

    private static PersonResource newPerson() {
        def resource = new PersonResource()
        resource.setNavn(new Personnavn(fornavn: 'Ola', etternavn: 'Nordmann'))
        return resource
    }

    /**
     * FintObjectFactory.newSchool() has no organisasjon link, which would make getTopOrganisation
     * short-circuit before it ever touches the organisation cache.
     */
    private static SkoleResource newSchool() {
        def resource = new SkoleResource()
        resource.setNavn('HVS')
        resource.setOrganisasjonsnummer(new Identifikator(identifikatorverdi: 'NO123456789'))
        resource.addOrganisasjon(new Link(verdi: ORGANISATION_HREF))
        resource.addSelf(new Link(verdi: 'link.to.School'))
        return resource
    }

    private static SkoleResources newSchoolResources() {
        def resources = new SkoleResources()
        resources.addResource(newSchool())
        return resources
    }

    /**
     * OrganisationRepository lowercases hrefs when it indexes, so the self link has to match
     * ORGANISATION_HREF after lowercasing. No overordnet link means this element is its own top
     * organisation.
     */
    private static OrganisasjonselementResources newOrganisationResources() {
        def resource = new OrganisasjonselementResource()
        resource.setNavn('Testfylke')
        resource.setOrganisasjonsnummer(new Identifikator(identifikatorverdi: 'NO987654321'))
        resource.addSelf(new Link(verdi: ORGANISATION_HREF))

        def resources = new OrganisasjonselementResources()
        resources.addResource(resource)
        return resources
    }
}
